package dev.zemco.schemalens.modeling.nodes

import dev.zemco.schemalens.modeling.fields.DataModelEdgeFieldEditDto
import dev.zemco.schemalens.modeling.fields.DataModelFieldDto
import dev.zemco.schemalens.modeling.fields.DataModelFieldInputDto
import dev.zemco.schemalens.validation.OnCreate
import dev.zemco.schemalens.validation.OnUpdate
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class DataModelNodeDto(
    val nodeId: Long,
    val name: String,
    val fields: List<DataModelFieldDto>,
) {
    companion object {
        fun from(node: DataModelNode): DataModelNodeDto =
            DataModelNodeDto(
                nodeId = node.id!!,
                name = node.name,
                fields = node.fields
                    .sortedBy { it.position }
                    .map { DataModelFieldDto.from(it) },
            )
    }
}

data class DataModelNodeDefinitionInputDto(
    @field:NotBlank(groups = [OnCreate::class, OnUpdate::class])
    val name: String,
    val fields: List<DataModelFieldInputDto>,
    @field:Size(max = 0, groups = [OnCreate::class])
    val edgeFields: List<DataModelEdgeFieldEditDto> = emptyList(),
)
