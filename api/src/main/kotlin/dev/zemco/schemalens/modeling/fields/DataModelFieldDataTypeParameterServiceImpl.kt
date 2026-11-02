package dev.zemco.schemalens.modeling.fields

import dev.zemco.schemalens.modeling.types.DataModelDataType
import org.springframework.stereotype.Service

@Service
class DataModelFieldDataTypeParameterServiceImpl : DataModelFieldDataTypeParameterService {

    override fun syncParameters(
        field: DataModelNodeField,
        dataType: DataModelDataType,
        requestedParameters: List<DataModelFieldDataTypeParameterInputDto>?,
        clearWhenAbsent: Boolean,
    ) {
        if (requestedParameters == null) {
            if (clearWhenAbsent) {
                field.typeParameters.clear()
            }
            return
        }

        val inputByName = normalizeByParameterName(requestedParameters)
        val availableParameters = dataType.parameters.associateBy { it.id.name.uppercase() }
        val unknownName = inputByName.keys.firstOrNull { it !in availableParameters }

        if (unknownName != null) {
            throw FieldDataTypeParameterUnknownException(inputByName.getValue(unknownName).name)
        }

        field.typeParameters.removeIf { existing ->
            existing.id.typeId != dataType.id || existing.id.name.uppercase() !in availableParameters
        }

        val existingByName = field.typeParameters.associateBy { it.id.name.uppercase() }
        val missingParameters = dataType.parameters.mapNotNull { parameter ->
            val value = inputByName[parameter.id.name.uppercase()]?.value
            val existingParameter = existingByName[parameter.id.name.uppercase()]

            if (existingParameter != null) {
                existingParameter.parameter = parameter
                existingParameter.value = value
                return@mapNotNull null
            }

            DataModelFieldDataTypeParameter(
                id = DataModelFieldDataTypeParameter.Id(
                    fieldId = field.id ?: 0,
                    typeId = dataType.id!!,
                    name = parameter.id.name,
                ),
                field = field,
                parameter = parameter,
                value = value,
            )
        }

        field.typeParameters.addAll(missingParameters)
    }

    private fun normalizeByParameterName(
        requestedParameters: List<DataModelFieldDataTypeParameterInputDto>,
    ): Map<String, DataModelFieldDataTypeParameterInputDto> {
        val inputByName = linkedMapOf<String, DataModelFieldDataTypeParameterInputDto>()

        for (input in requestedParameters) {
            val normalizedName = input.name.trim()
            val normalizedKey = normalizedName.uppercase()

            if (inputByName.put(normalizedKey, input.copy(name = normalizedName)) != null) {
                throw FieldDataTypeParameterExistsException(normalizedName)
            }
        }

        return inputByName
    }
}
