package dev.zemco.schemalens.modeling.fields

import dev.zemco.schemalens.modeling.nodes.DataModelNode
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotBlank

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "kind", length = 1)
abstract class DataModelField(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    var id: Long? = null,

    @Column(name = "node_id", nullable = false)
    var nodeId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id", nullable = false, insertable = false, updatable = false)
    var node: DataModelNode,

    @field:NotBlank
    @Column(nullable = false, length = 30)
    var name: String,

    @Column(nullable = false)
    var position: Short,
)
