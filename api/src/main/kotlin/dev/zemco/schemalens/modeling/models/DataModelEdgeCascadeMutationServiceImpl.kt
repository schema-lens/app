package dev.zemco.schemalens.modeling.models

import dev.zemco.schemalens.modeling.edges.DataModelEdge
import dev.zemco.schemalens.modeling.nodes.DataModelNode
import org.springframework.stereotype.Service

@Service
class DataModelEdgeCascadeMutationServiceImpl(
    private val cascadeService: DataModelCascadeService,
) : DataModelEdgeCascadeMutationService {

    override fun syncReferencedFieldsFromPrimaryKey(model: DataModel, edge: DataModelEdge) =
        cascadeService.syncReferencedFieldsFromPrimaryKey(model, edge)

    override fun collectCascadeEdgesAndSync(model: DataModel, fromNode: DataModelNode): List<DataModelEdge> =
        collectCascadeEdgesAndSync(model, listOf(fromNode))

    override fun collectCascadeEdgesAndSync(model: DataModel, fromNodes: Collection<DataModelNode>): List<DataModelEdge> =
        cascadeService.edgesInCascadeScope(fromNodes).also { scopedEdges ->
            cascadeService.syncReferencedFieldsFromPrimaryKey(model, scopedEdges)
        }
}
