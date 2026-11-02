package dev.zemco.schemalens.modeling.fields

import jakarta.validation.constraints.NotBlank

data class DataModelFieldDataTypeParameterInputDto(
    @field:NotBlank
    val name: String,
    val value: String? = null,
)

data class DataModelFieldDataTypeParameterDto(
    val name: String,
    val value: String? = null,
)

data class DataModelFieldInputDto(
    val fieldId: Long? = null,
    val name: String,
    val typeId: Long,
    val isPrimaryKey: Boolean,
    val isNullable: Boolean,
    val position: Short,
    val typeParameters: List<DataModelFieldDataTypeParameterInputDto>? = null,
)

data class DataModelFieldDto(
    val fieldId: Long? = null,
    val name: String,
    val typeId: Long,
    val isPrimaryKey: Boolean = false,
    val isNullable: Boolean = true,
    val position: Short,
    val typeParameters: List<DataModelFieldDataTypeParameterDto> = emptyList(),
) {
    companion object {
        fun from(field: DataModelNodeField): DataModelFieldDto {
            val parameters = field.type.parameters
            val typeParameters = if (parameters.isNotEmpty()) {
                val valuesByName = field.typeParameters.associateBy { it.id.name }
                parameters
                    .map { parameter ->
                        DataModelFieldDataTypeParameterDto(
                            name = parameter.id.name,
                            value = valuesByName[parameter.id.name]?.value,
                        )
                    }
                    .sortedBy { dto -> dto.name }
            } else emptyList()

            return DataModelFieldDto(
                fieldId = field.id,
                name = field.name,
                typeId = field.typeId,
                isPrimaryKey = field.isPrimaryKey,
                isNullable = field.isNullable,
                position = field.position,
                typeParameters = typeParameters,
            )
        }
    }
}

data class DataModelEdgeFieldDto(
    val fieldId: Long,
    val sourceNodeFieldId: Long,
    val parentFieldId: Long,
    val name: String,
    val position: Short,
)

data class DataModelEdgeFieldEditDto(
    val fieldId: Long,
    val name: String,
    val position: Short,
)
