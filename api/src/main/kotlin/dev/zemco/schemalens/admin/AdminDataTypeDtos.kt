package dev.zemco.schemalens.admin

import dev.zemco.schemalens.modeling.types.DataModelDataTypeMappingDto
import dev.zemco.schemalens.modeling.types.DataModelDataTypeParameterDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class AdminDataTypeParameterInputDto(
    @field:NotBlank
    @field:Size(max = 38)
    val name: String,
)

data class AdminDataTypeMappingInputDto(
    @field:Pattern(regexp = "(?i)oracle")
    val dbSystem: String,

    @field:NotBlank
    @field:Size(max = 38)
    val physicalDataType: String,

    val checkConstraint: String? = null,
)

data class AdminDataTypeInputDto(
    @field:NotBlank
    @field:Size(max = 40)
    val name: String,

    @field:Valid
    val parameters: List<AdminDataTypeParameterInputDto> = emptyList(),

    @field:Valid
    val mappings: List<AdminDataTypeMappingInputDto> = emptyList(),
)

data class AdminDataTypeDto(
    val typeId: Long,
    val name: String,
    val parameters: List<DataModelDataTypeParameterDto>,
    val mappings: List<DataModelDataTypeMappingDto>,
)
