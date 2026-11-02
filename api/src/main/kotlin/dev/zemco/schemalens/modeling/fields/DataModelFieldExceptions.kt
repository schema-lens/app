package dev.zemco.schemalens.modeling.fields

class DataModelNodeFieldEditRequestInvalidException(nodeId: Long) :
    RuntimeException("Field edits for node '$nodeId' must use unique identifiers of the corresponding direct or inherited fields on this node.")

class FieldNameInvalidException :
    RuntimeException("Field name must be nonblank and at most 30 UTF-8 bytes after trimming.")

class FieldNameNotUniqueException(name: String) :
    RuntimeException("Field with name '$name' already exists on this node!")

class FieldDataTypeParameterExistsException(name: String) :
    RuntimeException("Data type parameter with name '$name' was specified multiple times!")

class FieldDataTypeParameterUnknownException(name: String) :
    RuntimeException("Data type parameter with name '$name' does not exist on the selected data type!")

class DataModelEdgeReferencedFieldsImmutableException(edgeId: Long) :
    RuntimeException("Referenced fields and positions for edge '$edgeId' are immutable. Only field names and position can be updated.")
