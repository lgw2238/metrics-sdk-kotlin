package com.metrics.sdk

import com.metrics.sdk.monitoring.MetricsCollector
import com.metrics.sdk.monitoring.PrometheusMetricsCollector
import com.metrics.sdk.core.MetricsEngine
import com.metrics.sdk.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class MetricsSDK private constructor(
    private val config: MetricsSDKConfig,
    private val metricsCollector: MetricsCollector?,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val metricsEngine = MetricsEngine(scope, metricsCollector)

    companion object {
        /**
         * Create SDK instance with default configuration
         */
        fun create(): MetricsSDK = Builder().build()

        /**
         * Create SDK instance with custom configuration
         */
        fun create(block: Builder.() -> Unit): MetricsSDK = Builder().apply(block).build()
    }

    class Builder {
        private var enableMetrics: Boolean = false
        private var prometheusPort: Int? = null
        private var maxMatrices: Int = 10000
        private var operationTimeoutMs: Long = 30000

        fun enableMetrics(enable: Boolean = true) = apply { this.enableMetrics = enable }
        fun prometheusPort(port: Int) = apply { this.prometheusPort = port }
        fun maxMatrices(count: Int) = apply { this.maxMatrices = count }
        fun operationTimeoutMs(timeoutMs: Long) = apply { this.operationTimeoutMs = timeoutMs }

        fun build(): MetricsSDK {
            val config = MetricsSDKConfig(
                enableMetrics = enableMetrics,
                prometheusPort = prometheusPort,
                maxMatrices = maxMatrices,
                operationTimeoutMs = operationTimeoutMs
            )

            val metricsCollector = if (enableMetrics) {
                PrometheusMetricsCollector(prometheusPort)
            } else null

            return MetricsSDK(config, metricsCollector)
        }
    }

    /**
     * Create a new metrics
     */
    suspend fun createMetrics(metrics: MetricsData): Result<MetricsData> {
        return metricsEngine.createMetric(metrics)
    }

    /**
     * Get metrics by ID
     */
    suspend fun getMetric(id: String): Result<MetricsData?> {
        return metricsEngine.getMetric(id)
    }

    /**
     * Get all metrics
     */
    suspend fun getAllMetrics(): Result<List<MetricsData>> {
        return metricsEngine.getAllMetrics()
    }

    /**
     * Delete metrics by ID
     */
    suspend fun deleteMetrics(id: String): Result<Boolean> {
        return metricsEngine.deleteMetric(id)
    }

    /**
     * Add two metrics
     */
    suspend fun add(id1: String, id2: String, resultId: String? = null): Result<MetricsData> {
        return metricsEngine.add(id1, id2, resultId)
    }

    /**
     * Subtract two metrics
     */
    suspend fun subtract(id1: String, id2: String, resultId: String? = null): Result<MetricsData> {
        return metricsEngine.subtract(id1, id2, resultId)
    }

    /**
     * Multiply two metrics
     */
    suspend fun multiply(id1: String, id2: String, resultId: String? = null): Result<MetricsData> {
        return metricsEngine.multiply(id1, id2, resultId)
    }

    /**
     * Transpose a metrics
     */
    suspend fun transpose(id: String, resultId: String? = null): Result<MetricsData> {
        return metricsEngine.transpose(id, resultId)
    }

    /**
     * Calculate metric statistics
     */
    suspend fun calculateStatistics(id: String): Result<MetricsStatistics> {
        return metricsEngine.calculateStatistics(id)
    }

    /**
     * Cleanup resources
     */
    fun close() {
        metricsEngine.close()
        metricsCollector?.close()
    }

    /**
     * Get current metrics (if enabled)
     */
    fun getMetricsSnapshot(): String? {
        return metricsCollector?.getMetricsSnapshot()
    }
}