package dev.zemco.schemalens.modeling.edges

import dev.zemco.schemalens.modeling.fields.DataModelEdgeFieldDto
import dev.zemco.schemalens.validation.OnCreate
import dev.zemco.schemalens.validation.OnUpdate
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Null

data class DataModelEdgeDto(
    var edgeId: Long? = null,
    var modelId: Long,
    var fromNodeId: Long,
    var toNodeId: Long,
    var type: DataModelEdgeType,
    var isMandatory: Boolean,
    var isIdentifying: Boolean,
    val fields: List<DataModelEdgeFieldDto>,
) {
    companion object {
        fun from(edge: DataModelEdge): DataModelEdgeDto =
            DataModelEdgeDto(
                edgeId = edge.id!!,
                modelId = edge.modelId,
                fromNodeId = edge.fromNodeId,
                toNodeId = edge.toNodeId,
                type = edge.type,
                isMandatory = edge.isMandatory,
                isIdentifying = edge.isIdentifying,
                fields = edge.fields
                    .sortedBy { it.position }
                    .map {
                        DataModelEdgeFieldDto(
                            fieldId = it.id!!,
                            sourceNodeFieldId = it.sourceNodeField.id!!,
                            parentFieldId = it.parentField.id!!,
                            name = it.name,
                            position = it.position,
                        )
                    },
            )
    }
}

data class DataModelEdgeInputDto(
    var fromNodeId: Long,
    var toNodeId: Long,
    var type: DataModelEdgeType,
    var isMandatory: Boolean,
    var isIdentifying: Boolean,
    @field:Null(groups = [OnCreate::class])
    @field:NotNull(groups = [OnUpdate::class])
    val fields: List<DataModelEdgeFieldDto>? = null,
)
