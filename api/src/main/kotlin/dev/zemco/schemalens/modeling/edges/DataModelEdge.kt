package dev.zemco.schemalens.modeling.edges

import dev.zemco.schemalens.modeling.fields.DataModelEdgeField
import dev.zemco.schemalens.modeling.models.DataModel
import dev.zemco.schemalens.modeling.nodes.DataModelNode
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class DataModelEdge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edge_id")
    var id: Long? = null,

    @Column(name = "model_id", nullable = false)
    var modelId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false, insertable = false, updatable = false)
    var model: DataModel,

    @Column(name = "from_node_id", nullable = false)
    var fromNodeId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_node_id", nullable = false, insertable = false, updatable = false)
    var fromNode: DataModelNode,

    @Column(name = "to_node_id", nullable = false)
    var toNodeId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_node_id", nullable = false, insertable = false, updatable = false)
    var toNode: DataModelNode,

    @Column(nullable = false)
    @Convert(converter = DataModelEdgeTypeConverter::class)
    var type: DataModelEdgeType,

    @Column(nullable = false)
    var isMandatory: Boolean,

    @Column(nullable = false)
    var isIdentifying: Boolean,

    @OneToMany(
        mappedBy = "edge",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    var fields: MutableSet<DataModelEdgeField> = mutableSetOf(),
)
