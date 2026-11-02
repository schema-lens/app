package dev.zemco.schemalens.modeling.edges

import dev.zemco.schemalens.modeling.fields.DataModelEdgeReferencedFieldsImmutableException
import dev.zemco.schemalens.modeling.fields.validateFieldNames
import dev.zemco.schemalens.modeling.models.DataModel
import dev.zemco.schemalens.modeling.models.DataModelCascadeService
import dev.zemco.schemalens.modeling.models.DataModelEdgeCascadeMutationService
import dev.zemco.schemalens.modeling.models.DataModelModificationDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataModelEdgeServiceImpl(
    private val cascadeService: DataModelCascadeService,
    private val edgeRepository: DataModelEdgeRepository,
    private val edgeCascadeMutationService: DataModelEdgeCascadeMutationService,
    private val edgeWriteService: DataModelEdgeWriteService,
) : DataModelEdgeService {

    @Transactional
    override fun createEdge(model: DataModel, dto: DataModelEdgeInputDto): DataModelModificationDto {
        val fromNode = model.findNode(dto.fromNodeId)
        val toNode = model.findNode(dto.toNodeId)

        if (dto.isIdentifying) {
            cascadeService.checkNoIdentifyingCycle(model, fromNode.id!!, toNode.id!!, excludeEdgeId = null)
        }

        val edge = DataModelEdge(
            modelId = model.id!!,
            model = model,
            fromNodeId = fromNode.id!!,
            fromNode = fromNode,
            toNodeId = toNode.id!!,
            toNode = toNode,
            type = dto.type,
            isMandatory = dto.isMandatory,
            isIdentifying = dto.isIdentifying,
        )

        val saved = edgeRepository.save(edge)
        model.edges.add(saved)

        return persistUpdatedEdges(
            model = model,
            edge = saved,
            syncEdgeFields = true,
            syncCascade = saved.isIdentifying,
        )
    }

    @Transactional
    override fun updateEdge(model: DataModel, edgeId: Long, dto: DataModelEdgeInputDto): DataModelModificationDto {
        val edge = model.findEdge(edgeId)
        val wasIdentifying = edge.isIdentifying

        val requestedFields = dto.fields.orEmpty()
        val existingFieldsById = edge.fields.associateBy { it.id!! }
        val requestedFieldIds = requestedFields.map { it.fieldId }.toSet()

        if (requestedFieldIds.size != requestedFields.size || existingFieldsById.keys != requestedFieldIds) {
            throw DataModelEdgeReferencedFieldsImmutableException(edgeId)
        }

        if (dto.isIdentifying && !wasIdentifying) {
            cascadeService.checkNoIdentifyingCycle(model, edge.fromNodeId, edge.toNodeId, excludeEdgeId = edge.id)
        }

        requestedFields.forEach { field ->
            val existingField = existingFieldsById.getValue(field.fieldId)
            if (existingField.sourceNodeField.id != field.sourceNodeFieldId ||
                existingField.parentField.id != field.parentFieldId
            ) {
                throw DataModelEdgeReferencedFieldsImmutableException(edgeId)
            }
        }

        val requestedNames = requestedFields.associate { it.fieldId to it.name.trim() }
        val otherFieldNames = edge.toNode.fields.map { it.name } +
            model.edges
                .filter { it.toNodeId == edge.toNodeId && it.id != edgeId }
                .flatMap { incoming -> incoming.fields.map { it.name } }

        validateFieldNames(otherFieldNames + requestedNames.values)

        edge.apply {
            type = dto.type
            isMandatory = dto.isMandatory
            isIdentifying = dto.isIdentifying
        }
        requestedFields.forEach { field ->
            val existingField = existingFieldsById.getValue(field.fieldId)
            existingField.name = requestedNames.getValue(field.fieldId)
            existingField.position = field.position
        }

        return persistUpdatedEdges(
            model = model,
            edge = edge,
            syncEdgeFields = false,
            syncCascade = wasIdentifying != edge.isIdentifying,
        )
    }

    @Transactional
    override fun deleteEdge(model: DataModel, edgeId: Long): DataModelModificationDto {
        val edge = model.findEdge(edgeId)
        val deletedEdgeId = edge.id!!
        val wasIdentifying = edge.isIdentifying
        val toNodeId = edge.toNodeId

        model.edges.removeIf { it.id == deletedEdgeId }
        edgeRepository.delete(edge)

        if (!wasIdentifying) {
            return DataModelModificationDto(
                deletedEdgeIds = listOf(deletedEdgeId),
                visuallyStaleNodeIds = listOf(toNodeId),
            )
        }

        val cascadeEdges = edgeCascadeMutationService.collectCascadeEdgesAndSync(model, edge.toNode)
        val updatedEdges = edgeWriteService.persistAndMapEdges(cascadeEdges)
        val visuallyStaleNodeIds = (listOf(toNodeId) + updatedEdges.map { it.toNodeId }).distinct()

        return DataModelModificationDto(
            updatedEdges = updatedEdges,
            deletedEdgeIds = listOf(deletedEdgeId),
            visuallyStaleNodeIds = visuallyStaleNodeIds,
        )
    }

    private fun persistUpdatedEdges(
        model: DataModel,
        edge: DataModelEdge,
        syncEdgeFields: Boolean,
        syncCascade: Boolean,
    ): DataModelModificationDto {
        if (syncEdgeFields) {
            edgeCascadeMutationService.syncReferencedFieldsFromPrimaryKey(model, edge)
        }

        if (!syncCascade) {
            return DataModelModificationDto(
                updatedEdges = edgeWriteService.persistAndMapEdges(listOf(edge)),
                visuallyStaleNodeIds = listOf(edge.toNodeId),
            )
        }

        val cascadeEdges = edgeCascadeMutationService.collectCascadeEdgesAndSync(model, edge.toNode)

        val edgesToPersist = (sequenceOf(edge) + cascadeEdges.asSequence())
            .distinctBy { it.id }
            .toList()

        val updatedEdges = edgeWriteService.persistAndMapEdges(edgesToPersist)
        val visuallyStaleNodeIds = updatedEdges.map { it.toNodeId }.distinct()

        return DataModelModificationDto(
            updatedEdges = updatedEdges,
            visuallyStaleNodeIds = visuallyStaleNodeIds,
        )
    }
}
