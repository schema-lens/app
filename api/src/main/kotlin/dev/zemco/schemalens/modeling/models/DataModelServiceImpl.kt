package dev.zemco.schemalens.modeling.models

import dev.zemco.schemalens.auth.User
import dev.zemco.schemalens.modeling.nodes.DataModelNodeRepository
import dev.zemco.schemalens.modeling.types.DataModelDataType
import dev.zemco.schemalens.modeling.types.DataModelDataTypeOracle
import dev.zemco.schemalens.modeling.types.DataModelDataTypeParameter
import dev.zemco.schemalens.modeling.types.DataModelDataTypeRepository
import dev.zemco.schemalens.modeling.types.defaults.DataModelDefaultDataTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataModelServiceImpl(
    private val repository: DataModelRepository,
    private val nodeRepository: DataModelNodeRepository,
    private val dataTypeRepository: DataModelDataTypeRepository,
    private val defaultDataTypeRepository: DataModelDefaultDataTypeRepository,
) : DataModelService {

    override fun getAllModels(userId: Long): List<DataModelDto> =
        repository.findAllByOwnerId(userId).map { DataModelDto.from(it) }

    @Transactional
    override fun createModel(dto: DataModelInputDto, user: User): DataModelDto {
        val entity = DataModel(
            name = dto.name,
            ownerId = user.id!!,
            enabledContexts = dto.enabledContexts.toEntity(),
            owner = user,
        )
        val saved = repository.save(entity)
        createDefaultDataTypes(saved)
        return DataModelDto.from(saved)
    }

    private fun createDefaultDataTypes(model: DataModel) {
        defaultDataTypeRepository.findAll().forEach { default ->
            val dataType = dataTypeRepository.save(DataModelDataType(
                modelId = model.id!!,
                model = model,
                name = default.name,
            ))

            dataType.parameters.addAll(default.parameters.map { parameter ->
                DataModelDataTypeParameter(
                    id = DataModelDataTypeParameter.Id(dataType.id!!, parameter.id.name),
                    dataType = dataType,
                )
            })

            dataType.oracle = default.oracle?.let { mapping ->
                DataModelDataTypeOracle(
                    dataType = dataType,
                    physical = mapping.physical,
                    checkConstraint = mapping.checkConstraint,
                )
            }

            model.dataTypes.add(dataTypeRepository.save(dataType))
        }
    }

    @Transactional
    override fun updateModel(model: DataModel, dto: DataModelInputDto): DataModelDto {
        model.apply {
            name = dto.name
            enabledContexts = dto.enabledContexts.toEntity()
        }

        val saved = repository.save(model)
        return DataModelDto.from(saved)
    }

    @Transactional
    override fun deleteModel(modelId: Long) {
        nodeRepository.bulkDeleteByModelId(modelId)
        dataTypeRepository.bulkDeleteByModelId(modelId)
        repository.deleteById(modelId)
    }

    @Transactional(readOnly = true)
    override fun getModelDetails(model: DataModel): DataModelDetailsDto =
        DataModelDetailsDto.from(model)

    override fun getDataModelById(modelId: Long): DataModel? =
        repository.findModelTreeById(modelId)

    override fun getSecuredDataModelById(modelId: Long, user: User): DataModel? =
        repository.findModelTreeByIdAndOwnerId(modelId, user.id!!)
}
