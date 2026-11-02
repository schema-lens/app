package dev.zemco.schemalens.modeling.fields

internal fun validateFieldNames(fieldNames: List<String>) {
    val seen = mutableSetOf<String>()

    for (name in fieldNames) {
        val normalized = name.trim()

        if (normalized.isBlank() || normalized.toByteArray(Charsets.UTF_8).size > 30) {
            throw FieldNameInvalidException()
        }

        if (!seen.add(normalized.uppercase())) {
            throw FieldNameNotUniqueException(normalized)
        }
    }
}
