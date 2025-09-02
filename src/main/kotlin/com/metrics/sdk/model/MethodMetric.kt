package com.metrics.sdk.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class MethodMetric(
    val methodName: String,
    val parameters: Map<String, String>,
    val startTime: Long,
    val endTime: Long = 0,
    val executionTimeMs: Long = 0,
    val successful: Boolean = false,
    val errorMessage: String? = null,
    val domainId: String? = null,
    val traceId: String = generateTraceId(),
) {
    companion object {
        private fun generateTraceId(): String =
            "trace-${Instant.now().toEpochMilli()}-${(0..9999).random()}"
    }
}
