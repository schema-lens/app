package dev.zemco.schemalens.modeling.nodes

import dev.zemco.schemalens.modeling.edges.DataModelEdge
import dev.zemco.schemalens.modeling.edges.DataModelEdgeDto
import dev.zemco.schemalens.modeling.edges.DataModelEdgeWriteService
import dev.zemco.schemalens.modeling.fields.DataModelFieldDataTypeParameterService
import dev.zemco.schemalens.modeling.fields.DataModelFieldInputDto
import dev.zemco.schemalens.modeling.fields.DataModelNodeField
import dev.zemco.schemalens.modeling.fields.DataModelNodeFieldEditRequestInvalidException
import dev.zemco.schemalens.modeling.fields.validateFieldNames
import dev.zemco.schemalens.modeling.models.DataModel
import dev.zemco.schemalens.modeling.models.DataModelCascadeService
import dev.zemco.schemalens.modeling.models.DataModelEdgeCascadeMutationService
import dev.zemco.schemalens.modeling.models.DataModelModificationDto
import dev.zemco.schemalens.modeling.types.DataModelDataType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataModelNodeServiceImpl(
    private val nodeRepository: DataModelNodeRepository,
    private val cascadeService: DataModelCascadeService,
    private val edgeCascadeMutationService: DataModelEdgeCascadeMutationService,
    private val edgeWriteService: DataModelEdgeWriteService,
    private val fieldDataTypeParameterService: DataModelFieldDataTypeParameterService,
) : DataModelNodeService {

    private enum class PrimaryKeyChange { NONE, TYPE_ONLY, STRUCTURAL }

    @Transactional
    override fun createNode(model: DataModel, dto: DataModelNodeDefinitionInputDto): DataModelNodeDto {
        val normalizedName = dto.name.trim()

        if (model.findNodeByNameOrNull(normalizedName) != null) {
            throw NodeExistsException(normalizedName)
        }

        validateFieldNames(dto.fields.map { it.name })

        val node = nodeRepository.save(
            DataModelNode(
                modelId = model.id!!,
                model = model,
                name = normalizedName,
            )
        )

        node.fields = dto.fields.asSequence()
            .map { it.toEntityWithParameters(model, node) }
            .toMutableSet()

        return DataModelNodeDto.from(nodeRepository.save(node))
    }

    @Transactional
    override fun updateNodeDefinition(model: DataModel, nodeId: Long, dto: DataModelNodeDefinitionInputDto): DataModelModificationDto {
        val normalizedName = dto.name.trim()
        val existingNode = model.findNodeByNameOrNull(normalizedName)

        if (existingNode != null && existingNode.id != nodeId) {
            throw NodeExistsException(normalizedName)
        }

        val node = model.findNode(nodeId)
        val (toUpdate, toInsert) = dto.fields.partition { it.fieldId != null }
        val updateById = toUpdate.associateBy { it.fieldId!! }
        val existingFieldIds = node.fields.map { it.id!! }.toSet()

        if (updateById.size != toUpdate.size || !existingFieldIds.containsAll(updateById.keys)) {
            throw DataModelNodeFieldEditRequestInvalidException(nodeId)
        }

        val editedEdges = updateInheritedFields(model, nodeId, dto)

        node.name = normalizedName
        val primaryKeyChange = detectPrimaryKeyChange(model, node.fields, dto.fields)

        node.fields.forEach { existingField ->
            val input = updateById[existingField.id]

            if (input != null) {
                val dataType = model.findDataType(input.typeId)
                val typeChanged = existingField.typeId != input.typeId

                existingField.apply {
                    name = input.name.trim()
                    typeId = dataType.id!!
                    type = dataType
                    isPrimaryKey = input.isPrimaryKey
                    isNullable = input.isNullable
                    position = input.position
                }

                fieldDataTypeParameterService.syncParameters(
                    existingField, dataType, input.typeParameters, clearWhenAbsent = typeChanged,
                )
            }
        }

        node.fields.removeAll { it.id !in updateById }

        node.fields.addAll(
            toInsert.map { it.toEntityWithParameters(model, node) }
        )

        val savedNode = nodeRepository.save(node)
        val cascadeEdges = when (primaryKeyChange) {
            PrimaryKeyChange.NONE -> emptyList()
            PrimaryKeyChange.TYPE_ONLY -> cascadeService.edgesInCascadeScope(savedNode)
            PrimaryKeyChange.STRUCTURAL -> edgeCascadeMutationService.collectCascadeEdgesAndSync(model, savedNode)
        }

        val updatedCascadeEdges = if (primaryKeyChange == PrimaryKeyChange.STRUCTURAL) {
            edgeWriteService.persistAndMapEdges(cascadeEdges)
        } else {
            emptyList()
        }

        val updatedEdges = (updatedCascadeEdges + editedEdges.map { DataModelEdgeDto.from(it) })
            .distinctBy { it.edgeId }

        val cascadeNodeIds = cascadeEdges
            .map { it.toNodeId }
            .filter { it != savedNode.id }

        val visuallyStaleNodeIds = (cascadeNodeIds + editedEdges.map { it.toNodeId }).distinct()

        return DataModelModificationDto(
            updatedNodes = listOf(DataModelNodeDto.from(savedNode)),
            updatedEdges = updatedEdges,
            visuallyStaleNodeIds = visuallyStaleNodeIds,
        )
    }

    @Transactional
    override fun deleteNode(model: DataModel, nodeId: Long): DataModelModificationDto {
        val node = model.findNode(nodeId)

        val nodeEdges = model.edges
            .asSequence()
            .filter { it.fromNodeId == nodeId || it.toNodeId == nodeId }
            .toSet()

        val cascadeStartNodes = nodeEdges
            .asSequence()
            .filter { it.fromNodeId == nodeId && it.isIdentifying }
            .map { it.toNodeId }
            .distinct()
            .map { model.findNode(it) }
            .toList()

        val deletedEdgeIds = nodeEdges.map { it.id!! }

        val directlyAffectedNodeIds = nodeEdges
            .asSequence()
            .filter { it.fromNodeId == nodeId }
            .map { it.toNodeId }
            .toList()

        model.edges.removeAll(nodeEdges)
        model.nodes.remove(node)
        nodeRepository.delete(node)

        val cascadeEdges = edgeCascadeMutationService.collectCascadeEdgesAndSync(model, cascadeStartNodes)
        val updatedEdges = edgeWriteService.persistAndMapEdges(cascadeEdges)

        val visuallyStaleNodeIds = (directlyAffectedNodeIds + updatedEdges.map { it.toNodeId })
            .filter { it != nodeId }
            .distinct()

        return DataModelModificationDto(
            updatedEdges = updatedEdges,
            deletedNodeIds = listOf(nodeId),
            deletedEdgeIds = deletedEdgeIds,
            visuallyStaleNodeIds = visuallyStaleNodeIds,
        )
    }

    private fun updateInheritedFields(
        model: DataModel,
        nodeId: Long,
        dto: DataModelNodeDefinitionInputDto,
    ): List<DataModelEdge> {
        val incomingEdges = model.edges.filter { it.toNodeId == nodeId }
        val existingFieldsById = incomingEdges
            .flatMap { it.fields }
            .associateBy { it.id!! }
        val requestedFieldsById = dto.edgeFields.associateBy { it.fieldId }

        if (requestedFieldsById.size != dto.edgeFields.size ||
            !existingFieldsById.keys.containsAll(requestedFieldsById.keys)
        ) {
            throw DataModelNodeFieldEditRequestInvalidException(nodeId)
        }

        val edgeFieldNames = existingFieldsById.map { (id, field) -> requestedFieldsById[id]?.name ?: field.name }
        validateFieldNames(dto.fields.map { it.name } + edgeFieldNames)

        requestedFieldsById.forEach { (id, input) ->
            existingFieldsById.getValue(id).apply {
                name = input.name.trim()
                position = input.position
            }
        }

        return incomingEdges.filter { edge -> edge.fields.any { it.id in requestedFieldsById } }
    }

    private fun detectPrimaryKeyChange(
        model: DataModel,
        existingFields: Collection<DataModelNodeField>,
        requestedFields: Collection<DataModelFieldInputDto>,
    ): PrimaryKeyChange {
        val existingById = existingFields.associateBy { field -> field.id!! }

        val requestedById = requestedFields
            .asSequence()
            .filter { it.fieldId != null }
            .associateBy { it.fieldId!! }

        if (existingFields.any { it.isPrimaryKey && it.id !in requestedById }) {
            return PrimaryKeyChange.STRUCTURAL
        }

        if (requestedFields.any { it.fieldId == null && it.isPrimaryKey }) {
            return PrimaryKeyChange.STRUCTURAL
        }

        var typeOnlyChange = false

        for ((fieldId, existingField) in existingById) {
            val requestedField = requestedById[fieldId] ?: continue

            if (requestedField.isPrimaryKey != existingField.isPrimaryKey) {
                return PrimaryKeyChange.STRUCTURAL
            }

            if (existingField.isPrimaryKey && requestedField.typeId != existingField.typeId) {
                typeOnlyChange = true
            } else if (existingField.isPrimaryKey) {
                val requestedDataType = model.findDataType(requestedField.typeId)
                if (hasRequestedTypeParameterChange(existingField, requestedField, requestedDataType)) {
                    typeOnlyChange = true
                }
            }
        }

        return if (typeOnlyChange) PrimaryKeyChange.TYPE_ONLY else PrimaryKeyChange.NONE
    }

    private fun hasRequestedTypeParameterChange(
        existingField: DataModelNodeField,
        requestedField: DataModelFieldInputDto,
        requestedDataType: DataModelDataType,
    ): Boolean {
        val requestedParameters = requestedField.typeParameters ?: return false
        val existingValuesByName = existingField.typeParameters.associate { it.id.name.uppercase() to it.value }
        val requestedValuesByName = requestedParameters.associate { it.name.trim().uppercase() to it.value }

        return requestedDataType.parameters.any { parameter ->
            val key = parameter.id.name.uppercase()
            existingValuesByName[key] != requestedValuesByName[key]
        }
    }

    private fun DataModelFieldInputDto.toEntity(model: DataModel, node: DataModelNode): DataModelNodeField {
        val dataType = model.findDataType(typeId)

        if (isPrimaryKey && isNullable) {
            throw IllegalArgumentException("Primary key field cannot be nullable")
        }

        return DataModelNodeField(
            nodeId = node.id ?: 0,
            node = node,
            name = name.trim(),
            typeId = dataType.id!!,
            type = dataType,
            isPrimaryKey = isPrimaryKey,
            isNullable = isNullable,
            position = position,
        )
    }

    private fun DataModelFieldInputDto.toEntityWithParameters(model: DataModel, node: DataModelNode): DataModelNodeField =
        toEntity(model, node).also {
            fieldDataTypeParameterService.syncParameters(
                it, it.type, typeParameters, clearWhenAbsent = true,
            )
        }
}
