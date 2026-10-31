package dev.zemco.schemalens.modeling.types.defaults

import dev.zemco.schemalens.ResourceNotFoundException
import dev.zemco.schemalens.admin.AdminDataTypeDto
import dev.zemco.schemalens.admin.AdminDataTypeInputDto
import dev.zemco.schemalens.admin.AdminDataTypeMappingInputDto
import dev.zemco.schemalens.admin.AdminDataTypeParameterInputDto
import dev.zemco.schemalens.modeling.types.DataModelDataTypeMappingDto
import dev.zemco.schemalens.modeling.types.DataModelDataTypeParameterDto
import dev.zemco.schemalens.modeling.types.DataTypeExistsException
import dev.zemco.schemalens.modeling.types.DataTypeMappingExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataModelDefaultDataTypeServiceImpl(
    private val repository: DataModelDefaultDataTypeRepository,
) : DataModelDefaultDataTypeService {

    @Transactional(readOnly = true)
    override fun getDataTypes(): List<AdminDataTypeDto> =
        repository.findAll().sortedBy { it.name.uppercase() }.map { it.toDto() }

    @Transactional
    override fun createDataType(dto: AdminDataTypeInputDto): AdminDataTypeDto {
        val name = dto.name.trim()

        if (repository.existsByNameIgnoreCase(name)) {
            throw DataTypeExistsException(name)
        }

        val type = repository.save(DataModelDefaultDataType(name = name))
        type.sync(dto)
        return repository.save(type).toDto()
    }

    @Transactional
    override fun updateDataType(typeId: Long, dto: AdminDataTypeInputDto): AdminDataTypeDto {
        val type = findDataType(typeId)
        val name = dto.name.trim()

        if (repository.existsByNameIgnoreCaseAndIdNot(name, typeId)) {
            throw DataTypeExistsException(name)
        }

        type.name = name
        type.sync(dto)
        return repository.save(type).toDto()
    }

    @Transactional
    override fun deleteDataType(typeId: Long) {
        repository.delete(findDataType(typeId))
    }

    private fun findDataType(typeId: Long): DataModelDefaultDataType =
        repository.findById(typeId).orElseThrow {
            ResourceNotFoundException.withId("Default data type", typeId)
        }

    private fun DataModelDefaultDataType.sync(dto: AdminDataTypeInputDto) {
        syncParameters(dto.parameters)
        syncOracleMapping(dto.mappings)
    }

    private fun DataModelDefaultDataType.syncParameters(requestedParameters: List<AdminDataTypeParameterInputDto>) {
        val requestedNamesByKey = linkedMapOf<String, String>()

        requestedParameters.forEach { parameter ->
            val name = parameter.name.trim()

            if (requestedNamesByKey.putIfAbsent(name.uppercase(), name) != null) {
                throw DuplicateDefaultDataTypeParameterException(name)
            }
        }

        parameters.removeIf { it.id.name.uppercase() !in requestedNamesByKey }
        val retainedKeys = parameters.map { it.id.name.uppercase() }.toSet()

        requestedNamesByKey
            .asSequence()
            .filter { it.key !in retainedKeys }
            .map { it.value }
            .forEach { name -> parameters.add(DataModelDefaultDataTypeParameter(
                id = DataModelDefaultDataTypeParameter.Id(id!!, name),
                dataType = this,
            ))}
    }

    private fun DataModelDefaultDataType.syncOracleMapping(mappings: List<AdminDataTypeMappingInputDto>) {
        if (mappings.size > 1) {
            throw DataTypeMappingExistsException("oracle")
        }

        val mapping = mappings.singleOrNull()
        if (mapping == null) {
            oracle = null
            return
        }

        val physical = mapping.physicalDataType.trim()
        val checkConstraint = mapping.checkConstraint?.trim()?.takeIf { it.isNotEmpty() }
        oracle = oracle?.apply {
            this.physical = physical
            this.checkConstraint = checkConstraint
        } ?: DataModelDefaultDataTypeOracle(
            dataType = this,
            physical = physical,
            checkConstraint = checkConstraint,
        )
    }

    private fun DataModelDefaultDataType.toDto(): AdminDataTypeDto = AdminDataTypeDto(
        typeId = id!!,
        name = name,
        parameters = parameters.map { DataModelDataTypeParameterDto(it.id.name) }.sortedBy { it.name },
        mappings = oracle?.let { listOf(DataModelDataTypeMappingDto("oracle", it.physical, it.checkConstraint)) }
            ?: emptyList(),
    )
}
