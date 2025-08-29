package com.metrics.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class MetricsStatistics (
    val metricsId: String,
    val mean: Double,
    val standardDeviation: Double,
    val min: Double,
    val max: Double,
    val sum: Double,
    val variance: Double,
    val calculatedAt: Long = System.currentTimeMillis()
)