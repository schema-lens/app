package dev.zemco.schemalens.modeling.types.defaults

import dev.zemco.schemalens.admin.AdminDataTypeDto
import dev.zemco.schemalens.admin.AdminDataTypeInputDto

interface DataModelDefaultDataTypeService {
    fun getDataTypes(): List<AdminDataTypeDto>
    fun createDataType(dto: AdminDataTypeInputDto): AdminDataTypeDto
    fun updateDataType(typeId: Long, dto: AdminDataTypeInputDto): AdminDataTypeDto
    fun deleteDataType(typeId: Long)
}
