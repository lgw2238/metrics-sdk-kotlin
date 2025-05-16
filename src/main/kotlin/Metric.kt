package com.example.metrics

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val domain: String,
    val itemId: String,
    val userId: String,
    @EncodeDefault val timestamp: Long = System.currentTimeMillis()
) {
    constructor(domain: String, itemId: String, userId: String, timestamp: Long? = null) : this(
        domain,
        itemId,
        userId,
        timestamp ?: System.currentTimeMillis()
    )
}