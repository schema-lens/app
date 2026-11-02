package dev.zemco.schemalens.modeling.fields

import dev.zemco.schemalens.modeling.types.DataModelDataTypeParameter
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
class DataModelFieldDataTypeParameter(
    @EmbeddedId
    var id: Id,

    @MapsId("fieldId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    var field: DataModelNodeField,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns(
        JoinColumn(name = "type_id", referencedColumnName = "type_id", nullable = false, insertable = false, updatable = false),
        JoinColumn(name = "name", referencedColumnName = "name", nullable = false, insertable = false, updatable = false),
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    var parameter: DataModelDataTypeParameter,

    @Column(name = "value")
    var value: String? = null,
) {
    @Embeddable
    data class Id(
        @Column(name = "field_id", nullable = false)
        var fieldId: Long = 0,

        @Column(name = "type_id", nullable = false)
        var typeId: Long = 0,

        @Column(name = "name", nullable = false, length = 38)
        var name: String = "",
    )
}
