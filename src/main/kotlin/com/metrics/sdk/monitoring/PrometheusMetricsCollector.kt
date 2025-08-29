package com.metrics.sdk.monitoring

import com.metrics.sdk.model.MetricsStatistics
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.HTTPServer
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class PrometheusMetricsCollector(private val port: Int?) : MetricsCollector {
    private val logger = LoggerFactory.getLogger(PrometheusMetricsCollector::class.java)
    private val registry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val httpServer: HTTPServer?

    private val operationTimer = Timer.builder("metrics_operation_duration")
        .description("Time taken for metrics operations")
        .register(registry)

    init {
        // Register JVM metrics
        JvmMemoryMetrics().bindTo(registry)
        JvmGcMetrics().bindTo(registry)
        ProcessorMetrics().bindTo(registry)

        // Start HTTP server if port is specified
        httpServer = port?.let { p ->
            try {
                HTTPServer("0.0.0.0", p, true)
            } catch (e: Exception) {
                logger.warn("Failed to start Prometheus HTTP server on port $p", e)
                null
            }
        }

        logger.info("PrometheusMetricsCollector initialized${port?.let { " with HTTP server on port $it" } ?: ""}")
    }

    override fun recordOperation(operationType: String, durationMs: Long) {
        Timer.builder("metrics_operation_duration")
            .tag("operation", operationType)
            .register(registry)
            .record(durationMs, TimeUnit.MILLISECONDS)

        registry.counter("metrics_operations_total", "operation", operationType).increment()
    }

    override fun recordMetricsCount(count: Int) {
        registry.gauge("metrics_count", count.toDouble())
    }

    override fun recordMetricsSize(metricsId: String, size: Int) {
        registry.gauge(metricsId, size.toDouble())
    }

    override fun recordMetricsStats(metricsId: String, stats: MetricsStatistics) {
        registry.gauge("metrics_mean", listOf(Tag.of("metrics_id", metricsId)), stats.mean)
        registry.gauge("metrics_std", listOf(Tag.of("metrics_id", metricsId)),  stats.standardDeviation)
        registry.gauge("metrics_min", listOf(Tag.of("metrics_id", metricsId)), stats.min)
        registry.gauge("metrics_max", listOf(Tag.of("metrics_id", metricsId)), stats.max)
        registry.gauge("metrics_variance", listOf(Tag.of("metrics_id", metricsId)), stats.variance)
    }

    override fun getMetricsSnapshot(): String {
        return registry.scrape()
    }

    override fun close() {
        httpServer?.close()
        logger.info("PrometheusMetricsCollector closed")
    }
}