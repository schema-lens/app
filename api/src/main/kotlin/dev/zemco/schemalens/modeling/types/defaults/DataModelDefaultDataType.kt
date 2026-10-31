package dev.zemco.schemalens.modeling.types.defaults

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.validation.constraints.NotBlank

@Entity
class DataModelDefaultDataType(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    @Column(name = "type_id")
    var id: Long? = null,

    @field:NotBlank
    @Column(nullable = false, length = 40)
    var name: String,

    @OneToOne(mappedBy = "dataType", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var oracle: DataModelDefaultDataTypeOracle? = null,

    @OneToMany(mappedBy = "dataType", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var parameters: MutableSet<DataModelDefaultDataTypeParameter> = mutableSetOf(),
)
