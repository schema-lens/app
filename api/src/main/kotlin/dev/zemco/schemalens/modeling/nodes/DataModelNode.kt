package dev.zemco.schemalens.modeling.nodes

import dev.zemco.schemalens.modeling.edges.DataModelEdge
import dev.zemco.schemalens.modeling.fields.DataModelNodeField
import dev.zemco.schemalens.modeling.models.DataModel
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
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
class DataModelNode(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    @Column(name = "node_id")
    var id: Long? = null,

    @Column(name = "model_id", nullable = false)
    var modelId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false, insertable = false, updatable = false)
    var model: DataModel,

    @field:NotBlank
    @Column(nullable = false, length = 30)
    var name: String,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "node", orphanRemoval = true, cascade = [CascadeType.ALL])
    var fields: MutableSet<DataModelNodeField> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fromNode", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var outgoingEdges: MutableSet<DataModelEdge> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "toNode", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var incomingEdges: MutableSet<DataModelEdge> = mutableSetOf(),
)
