package com.metrics.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class MethodMetricsReport(
    val domainId: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalCalls: Int,
    val successfulCalls: Int,
    val failedCalls: Int,
    val averageExecutionTimeMs: Double,
    val maxExecutionTimeMs: Long,
    val methodCounts: Map<String, Int>,
)
