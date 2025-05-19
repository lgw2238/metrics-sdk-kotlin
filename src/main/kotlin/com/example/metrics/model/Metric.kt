package com.example.metrics.model

import com.example.metrics.exception.EmptyFieldException
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val domain: String,
    val itemId: String,
    val userId: String,
    @EncodeDefault val timestamp: Long = System.currentTimeMillis()
) : EmptyFieldException {
    constructor(domain: String, itemId: String, userId: String, timestamp: Long? = null) : this(
        domain,
        itemId,
        userId,
        timestamp ?: System.currentTimeMillis()
    )

    // Optional field
    fun validateFields() {
        listOf("domain" to domain, "itemId" to itemId, "userId" to userId).forEach { (fieldName, value) ->
            if (value.isBlank()) throw IllegalArgumentException(getEmptyFieldMessage(fieldName))
        }
    }
}
