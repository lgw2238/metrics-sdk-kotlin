package com.metrics.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class MetricsSDKConfig(
    val enableMetrics: Boolean = false,
    val prometheusPort: Int? = null,
    val maxMatrices: Int = 10000,
    val operationTimeoutMs: Long = 30000,
)