package dev.zemco.schemalens.modeling.types.defaults

class DuplicateDefaultDataTypeParameterException(name: String) :
    RuntimeException("Duplicate data type parameter name '$name' in request!")
