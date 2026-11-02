package dev.zemco.schemalens.modeling.models

import dev.zemco.schemalens.modeling.edges.DataModelEdge
import dev.zemco.schemalens.modeling.nodes.DataModelNode

interface DataModelCascadeService {
    fun edgesInCascadeScope(startNode: DataModelNode): List<DataModelEdge>
    fun edgesInCascadeScope(startNodes: Collection<DataModelNode>): List<DataModelEdge>
    fun syncReferencedFieldsFromPrimaryKey(model: DataModel, edge: DataModelEdge)
    fun syncReferencedFieldsFromPrimaryKey(model: DataModel, edges: List<DataModelEdge>)
    fun checkNoIdentifyingCycle(model: DataModel, fromNodeId: Long, toNodeId: Long, excludeEdgeId: Long?)
}
