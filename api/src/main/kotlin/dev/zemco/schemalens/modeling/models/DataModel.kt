package dev.zemco.schemalens.modeling.models

import dev.zemco.schemalens.ResourceNotFoundException
import dev.zemco.schemalens.auth.User
import dev.zemco.schemalens.modeling.diagrams.DataModelDiagram
import dev.zemco.schemalens.modeling.edges.DataModelEdge
import dev.zemco.schemalens.modeling.nodes.DataModelNode
import dev.zemco.schemalens.modeling.types.DataModelDataType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.NotBlank

@Entity
class DataModel(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    @Column(name = "model_id")
    var id: Long? = null,

    @field:NotBlank
    @Column(nullable = false, length = 64)
    var name: String,

    @Column(name = "owner_id", nullable = false)
    var ownerId: Long,

    @Embedded
    var enabledContexts: DataModelEnabledContexts = DataModelEnabledContexts(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, insertable = false, updatable = false)
    var owner: User,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "model", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var dataTypes: MutableSet<DataModelDataType> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "model", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var nodes: MutableSet<DataModelNode> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "model", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var edges: MutableSet<DataModelEdge> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "model", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var diagrams: MutableSet<DataModelDiagram> = mutableSetOf(),
) {
    fun findNodeOrNull(nodeId: Long): DataModelNode? =
        nodes.firstOrNull { it.id == nodeId }

    fun findNode(nodeId: Long): DataModelNode =
        findNodeOrNull(nodeId) ?: throw ResourceNotFoundException.withId("Node", nodeId)

    fun findEdgeOrNull(edgeId: Long): DataModelEdge? =
        edges.firstOrNull { it.id == edgeId }

    fun findEdge(edgeId: Long): DataModelEdge =
        findEdgeOrNull(edgeId) ?: throw ResourceNotFoundException.withId("Edge", edgeId)

    fun findDataTypeOrNull(typeId: Long): DataModelDataType? =
        dataTypes.firstOrNull { it.id == typeId }

    fun findDataType(typeId: Long): DataModelDataType =
        findDataTypeOrNull(typeId) ?: throw ResourceNotFoundException.withId("Data type", typeId)

    fun findNodeByNameOrNull(name: String): DataModelNode? =
        nodes.firstOrNull { it.name.equals(name, ignoreCase = true) }

    fun findDataTypeByNameOrNull(name: String): DataModelDataType? =
        dataTypes.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
