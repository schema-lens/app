package dev.zemco.schemalens.modeling.fields

import dev.zemco.schemalens.modeling.types.DataModelDataType

interface DataModelFieldDataTypeParameterService {
    fun syncParameters(
        field: DataModelNodeField,
        dataType: DataModelDataType,
        requestedParameters: List<DataModelFieldDataTypeParameterInputDto>?,
        clearWhenAbsent: Boolean,
    )
}
