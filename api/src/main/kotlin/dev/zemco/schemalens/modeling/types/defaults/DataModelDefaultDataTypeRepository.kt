package dev.zemco.schemalens.modeling.types.defaults

import org.springframework.data.jpa.repository.JpaRepository

interface DataModelDefaultDataTypeRepository : JpaRepository<DataModelDefaultDataType, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean
}
