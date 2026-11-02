package dev.zemco.schemalens.modeling.fields

import dev.zemco.schemalens.modeling.nodes.DataModelNode
import dev.zemco.schemalens.modeling.types.DataModelDataType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
@DiscriminatorValue("N")
class DataModelNodeField(
    id: Long? = null,
    nodeId: Long,
    node: DataModelNode,
    name: String,

    @Column(name = "type_id", nullable = false)
    var typeId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false, insertable = false, updatable = false)
    var type: DataModelDataType,

    @Column(nullable = false)
    var isPrimaryKey: Boolean,

    @Column(nullable = false)
    var isNullable: Boolean,

    position: Short,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "field", cascade = [CascadeType.ALL], orphanRemoval = true)
    var typeParameters: MutableSet<DataModelFieldDataTypeParameter> = mutableSetOf(),
) : DataModelField(id, nodeId, node, name, position)
