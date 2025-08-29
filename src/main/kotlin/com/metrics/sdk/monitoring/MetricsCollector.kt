package com.metrics.sdk.monitoring

import com.metrics.sdk.model.MetricsStatistics

interface MetricsCollector {
    fun recordOperation(operationType: String, durationMs: Long)
    fun recordMetricsCount(count: Int)
    fun recordMetricsSize(metricsId: String, size: Int)
    fun recordMetricsStats(metricsId: String, stats: MetricsStatistics)
    fun getMetricsSnapshot(): String
    fun close()
}