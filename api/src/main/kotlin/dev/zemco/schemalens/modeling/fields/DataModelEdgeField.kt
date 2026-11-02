package dev.zemco.schemalens.modeling.fields

import dev.zemco.schemalens.modeling.edges.DataModelEdge
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
@DiscriminatorValue("E")
class DataModelEdgeField(
    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "edge_id", nullable = false, updatable = false)
    var edge: DataModelEdge,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_node_field_id", nullable = false, updatable = false)
    var sourceNodeField: DataModelNodeField,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_field_id", nullable = false, updatable = false)
    var parentField: DataModelField,

    name: String,
    position: Short,
) : DataModelField(id, edge.toNodeId, edge.toNode, name, position)
