package dev.zemco.schemalens.modeling.models

import dev.zemco.schemalens.ResourceNotFoundException
import dev.zemco.schemalens.modeling.edges.DataModelEdge
import dev.zemco.schemalens.modeling.edges.DataModelEdgeIdentifyingCycleException
import dev.zemco.schemalens.modeling.fields.DataModelEdgeField
import dev.zemco.schemalens.modeling.fields.DataModelField
import dev.zemco.schemalens.modeling.fields.DataModelNodeField
import dev.zemco.schemalens.modeling.nodes.DataModelNode
import java.util.PriorityQueue
import java.util.TreeMap
import org.springframework.stereotype.Service

@Service
class DataModelCascadeServiceImpl : DataModelCascadeService {

    override fun edgesInCascadeScope(startNode: DataModelNode): List<DataModelEdge> =
        edgesInCascadeScope(listOf(startNode))

    override fun edgesInCascadeScope(startNodes: Collection<DataModelNode>): List<DataModelEdge> {
        if (startNodes.isEmpty()) {
            return emptyList()
        }

        val model = startNodes.first().model

        val result = mutableListOf<DataModelEdge>()
        val visitedNodeIds = mutableSetOf<Long>()
        val outgoingEdgesByFromNodeId = model.edges.groupBy { it.fromNodeId }

        val queue = ArrayDeque<Long>()
        startNodes.forEach { queue.addLast(it.id!!) }

        while (queue.isNotEmpty()) {
            val currentNodeId = queue.removeFirst()

            if (!visitedNodeIds.add(currentNodeId)) {
                continue
            }

            val outgoingEdges = outgoingEdgesByFromNodeId[currentNodeId].orEmpty()

            outgoingEdges.forEach { edge ->
                result.add(edge)

                if (edge.isIdentifying) {
                    queue.addLast(edge.toNodeId)
                }
            }
        }

        return orderEdgesByIdentifyingDependencies(result, visitedNodeIds)
    }

    private fun orderEdgesByIdentifyingDependencies(
        edges: List<DataModelEdge>,
        nodeIds: Set<Long>,
    ): List<DataModelEdge> {
        val outgoingEdgesByFromNodeId = edges.groupBy { it.fromNodeId }
        val incomingIdentifyingCounts = nodeIds.associateWith { 0 }.toMutableMap()

        edges.asSequence().filter { it.isIdentifying }.forEach { edge ->
            incomingIdentifyingCounts[edge.toNodeId] = incomingIdentifyingCounts.getValue(edge.toNodeId) + 1
        }

        val readyNodeIds = PriorityQueue(incomingIdentifyingCounts.filterValues { it == 0 }.keys)
        val orderedEdges = mutableListOf<DataModelEdge>()
        var processedNodeCount = 0

        while (readyNodeIds.isNotEmpty()) {
            val nodeId = readyNodeIds.remove()
            processedNodeCount++

            for (edge in outgoingEdgesByFromNodeId[nodeId].orEmpty().sortedBy { it.id }) {
                orderedEdges.add(edge)
                if (edge.isIdentifying) {
                    val remaining = incomingIdentifyingCounts.getValue(edge.toNodeId) - 1
                    incomingIdentifyingCounts[edge.toNodeId] = remaining
                    if (remaining == 0) {
                        readyNodeIds.add(edge.toNodeId)
                    }
                }
            }
        }

        if (processedNodeCount != nodeIds.size) {
            throw DataModelEdgeIdentifyingCycleException()
        }

        return orderedEdges
    }

    override fun checkNoIdentifyingCycle(
        model: DataModel,
        fromNodeId: Long,
        toNodeId: Long,
        excludeEdgeId: Long?,
    ) {
        if (fromNodeId == toNodeId) {
            throw DataModelEdgeIdentifyingCycleException()
        }

        val outgoingIdentifyingByFromNodeId = model.edges.asSequence()
            .filter { it.isIdentifying && it.id != excludeEdgeId }
            .groupBy { it.fromNodeId }

        val visited = mutableSetOf<Long>()
        val queue = ArrayDeque<Long>()
        queue.addLast(toNodeId)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            if (!visited.add(current)) {
                continue
            }
            if (current == fromNodeId) {
                throw DataModelEdgeIdentifyingCycleException()
            }

            outgoingIdentifyingByFromNodeId[current].orEmpty().forEach {
                queue.addLast(it.toNodeId)
            }
        }
    }

    override fun syncReferencedFieldsFromPrimaryKey(model: DataModel, edge: DataModelEdge) =
        syncReferencedFieldsFromPrimaryKey(model, listOf(edge))

    override fun syncReferencedFieldsFromPrimaryKey(model: DataModel, edges: List<DataModelEdge>) {
        if (edges.isEmpty()) {
            return
        }

        val context = SynchronizationContext(model)
        edges.forEach { syncReferencedFieldsFromPrimaryKey(model, it, context) }
    }

    private fun syncReferencedFieldsFromPrimaryKey(
        model: DataModel,
        edge: DataModelEdge,
        context: SynchronizationContext,
    ) {
        val primaryKeyFields = context.primaryKeyFields(edge.fromNodeId)
        val unmatchedFieldsByParent = edge.fields.associateBy { it.parentField.identity() }.toMutableMap()
        val targetFields = context.targetFields(edge.toNodeId)
        val indexedEdge = context.containsEdge(edge)
        var nextPosition = targetFields.nextPosition()

        if (indexedEdge) {
            edge.fields.forEach { targetFields.remove(it) }
        }

        primaryKeyFields.forEach { (parent, _) ->
            unmatchedFieldsByParent[parent.identity()]?.let { targetFields.add(it) }
        }

        for ((parent, source) in primaryKeyFields) {
            if (unmatchedFieldsByParent.remove(parent.identity()) != null) {
                continue
            }

            require(parent.nodeId == edge.fromNodeId && source.node.modelId == model.id)

            val uniqueName = resolveUniqueFieldName(parent.name, targetFields)
            edge.fields.add(
                DataModelEdgeField(
                    edge = edge,
                    sourceNodeField = source,
                    parentField = parent,
                    name = uniqueName,
                    position = nextPosition++,
                ).also { targetFields.add(it) }
            )
        }

        edge.fields.removeAll(unmatchedFieldsByParent.values.toSet())
        if (!indexedEdge) {
            edge.fields.forEach { targetFields.remove(it) }
        }
        if (edge.isIdentifying) {
            context.invalidatePrimaryKeyFields(edge.toNodeId)
        }
    }

    private fun DataModelField.identity(): Any = id ?: this

    private fun resolveUniqueFieldName(baseName: String, targetFields: TargetFields): String {
        if (!targetFields.containsName(baseName)) {
            return baseName
        }

        var suffix = 2
        while (true) {
            val nameSuffix = "_$suffix"
            val candidate = baseName.take(MAX_FIELD_NAME_LENGTH - nameSuffix.length) + nameSuffix

            if (!targetFields.containsName(candidate)) {
                return candidate
            }
            suffix++
        }
    }

    private fun getSourcePrimaryKeyFields(node: DataModelNode, incomingEdges: List<DataModelEdge>): List<SourcePrimaryKeyField> {
        val directPrimaryKeyFields = node.fields
            .asSequence()
            .filter { it.isPrimaryKey }
            .map { SourcePrimaryKeyField(parent = it, source = it) }

        val identifyingEdgeFields = incomingEdges
            .asSequence()
            .filter { it.isIdentifying }
            .flatMap { it.fields }
            .map { SourcePrimaryKeyField(parent = it, source = it.sourceNodeField) }

        return (directPrimaryKeyFields + identifyingEdgeFields)
            .sortedWith(compareBy({ it.parent.position }, { it.parent.id }))
            .toList()
    }

    private inner class SynchronizationContext(model: DataModel) {
        private val nodesById = model.nodes.associateBy { it.id }
        private val indexedEdges = model.edges.toSet()
        private val incomingEdgesByNodeId = indexedEdges.groupBy { it.toNodeId }
        private val primaryKeyFieldsByNodeId = mutableMapOf<Long, List<SourcePrimaryKeyField>>()
        private val targetFieldsByNodeId = mutableMapOf<Long, TargetFields>()

        private fun node(nodeId: Long): DataModelNode =
            nodesById[nodeId] ?: throw ResourceNotFoundException.withId("Node", nodeId)

        fun containsEdge(edge: DataModelEdge): Boolean = edge in indexedEdges

        fun primaryKeyFields(nodeId: Long): List<SourcePrimaryKeyField> =
            primaryKeyFieldsByNodeId.getOrPut(nodeId) {
                getSourcePrimaryKeyFields(node(nodeId), incomingEdgesByNodeId[nodeId].orEmpty())
            }

        fun invalidatePrimaryKeyFields(nodeId: Long) {
            primaryKeyFieldsByNodeId.remove(nodeId)
        }

        fun targetFields(nodeId: Long): TargetFields =
            targetFieldsByNodeId.getOrPut(nodeId) {
                TargetFields().also { fields ->
                    node(nodeId).fields.forEach { fields.add(it) }
                    incomingEdgesByNodeId[nodeId].orEmpty().forEach { edge ->
                        edge.fields.forEach { fields.add(it) }
                    }
                }
            }
    }

    private class TargetFields {
        private val nameCounts = mutableMapOf<String, Int>()
        private val positionCounts = TreeMap<Short, Int>()

        fun containsName(name: String): Boolean = name.uppercase() in nameCounts

        fun nextPosition(): Short = positionCounts.lastEntry()?.key?.plus(1)?.toShort() ?: 1

        fun add(field: DataModelField) {
            nameCounts.merge(field.name.uppercase(), 1, Int::plus)
            positionCounts.merge(field.position, 1, Int::plus)
        }

        fun remove(field: DataModelField) {
            nameCounts.computeIfPresent(field.name.uppercase()) { _, count -> (count - 1).takeIf { it > 0 } }
            positionCounts.computeIfPresent(field.position) { _, count -> (count - 1).takeIf { it > 0 } }
        }
    }

    private data class SourcePrimaryKeyField(val parent: DataModelField, val source: DataModelNodeField)

    private companion object {
        const val MAX_FIELD_NAME_LENGTH = 30
    }
}
