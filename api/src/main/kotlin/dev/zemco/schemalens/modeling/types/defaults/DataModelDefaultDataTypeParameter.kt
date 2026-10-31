package dev.zemco.schemalens.modeling.types.defaults

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.validation.constraints.NotBlank

@Entity
class DataModelDefaultDataTypeParameter(
    @EmbeddedId
    var id: Id,

    @MapsId("typeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    var dataType: DataModelDefaultDataType,
) {
    @Embeddable
    data class Id(
        @Column(name = "type_id", nullable = false)
        var typeId: Long = 0,

        @field:NotBlank
        @Column(name = "name", nullable = false, length = 38)
        var name: String = "",
    )
}
