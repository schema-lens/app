package dev.zemco.schemalens.modeling.nodes

import dev.zemco.schemalens.modeling.models.DataModel
import dev.zemco.schemalens.modeling.models.DataModelModificationDto

interface DataModelNodeService {
    fun createNode(model: DataModel, dto: DataModelNodeDefinitionInputDto): DataModelNodeDto
    fun updateNodeDefinition(model: DataModel, nodeId: Long, dto: DataModelNodeDefinitionInputDto): DataModelModificationDto
    fun deleteNode(model: DataModel, nodeId: Long): DataModelModificationDto
}
