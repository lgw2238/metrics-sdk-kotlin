package com.metrics.sdk.core

import com.metrics.sdk.monitoring.MetricsCollector
import com.metrics.sdk.model.MetricsData
import com.metrics.sdk.model.MetricsStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class MetricsEngine   (
    private val scope: CoroutineScope,
    private val metricsCollector: MetricsCollector?
) {
    private val logger = LoggerFactory.getLogger(MetricsEngine::class.java)
    private val metrics = mutableMapOf<String, MetricsData>()
    private val mutex = Mutex()

    suspend fun createMetric(metric: MetricsData): Result<MetricsData> = mutex.withLock {
        return try {
            val validationResult = metric.validate()
            if (validationResult.isFailure) {
                return validationResult.map { metric }
            }

            val duration = measureTimeMillis {
                metrics[metric.id] = metric
            }

            metricsCollector?.recordOperation("create", duration)
            metricsCollector?.recordMetricsCount(metrics.size)
            metricsCollector?.recordMetricsSize(metric.id, metric.rows * metric.columns)

            logger.debug("Created metric {} with size {}x{}", metric.id, metric.rows, metric.columns)
            Result.success(metric)
        } catch (e: Exception) {
            logger.error("Failed to create metric ${metric.id}", e)
            Result.failure(e)
        }
    }

    suspend fun getMetric(id: String): Result<MetricsData?> = mutex.withLock {
        return try {
            val metric = metrics[id]
            Result.success(metric)
        } catch (e: Exception) {
            logger.error("Failed to get metric $id", e)
            Result.failure(e)
        }
    }

    suspend fun getAllMetrics(): Result<List<MetricsData>> = mutex.withLock {
        return try {
            Result.success(metrics.values.toList())
        } catch (e: Exception) {
            logger.error("Failed to get all metrics", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMetric(id: String): Result<Boolean> = mutex.withLock {
        return try {
            val removed = metrics.remove(id) != null
            if (removed) {
                metricsCollector?.recordMetricsCount(metrics.size)
                logger.debug("Deleted metric {}", id)
            }
            Result.success(removed)
        } catch (e: Exception) {
            logger.error("Failed to delete metric $id", e)
            Result.failure(e)
        }
    }

    suspend fun add(id1: String, id2: String, resultId: String?): Result<MetricsData> {
        return performBinaryOperation("add", id1, id2, resultId) { m1, m2, resId ->
            if (m1.rows != m2.rows || m1.columns != m2.columns) {
                throw IllegalArgumentException("Matrices must have same dimensions for addition")
            }

            val resultData = m1.data.mapIndexed { i, row ->
                row.mapIndexed { j, value ->
                    value + m2.data[i][j]
                }
            }

            MetricsData(
                id = resId,
                name = "${m1.name} + ${m2.name}",
                rows = m1.rows,
                columns = m1.columns,
                data = resultData
            )
        }
    }

    suspend fun subtract(id1: String, id2: String, resultId: String?): Result<MetricsData> {
        return performBinaryOperation("subtract", id1, id2, resultId) { m1, m2, resId ->
            if (m1.rows != m2.rows || m1.columns != m2.columns) {
                throw IllegalArgumentException("Matrices must have same dimensions for subtraction")
            }

            val resultData = m1.data.mapIndexed { i, row ->
                row.mapIndexed { j, value ->
                    value - m2.data[i][j]
                }
            }

            MetricsData(
                id = resId,
                name = "${m1.name} - ${m2.name}",
                rows = m1.rows,
                columns = m1.columns,
                data = resultData
            )
        }
    }

    suspend fun multiply(id1: String, id2: String, resultId: String?): Result<MetricsData> {
        return performBinaryOperation("multiply", id1, id2, resultId) { m1, m2, resId ->
            if (m1.columns != m2.rows) {
                throw IllegalArgumentException("First metric columns must equal second metric rows")
            }

            val resultData = Array(m1.rows) { Array(m2.columns) { 0.0 } }

            for (i in 0 until m1.rows) {
                for (j in 0 until m2.columns) {
                    for (k in 0 until m1.columns) {
                        resultData[i][j] += m1.data[i][k] * m2.data[k][j]
                    }
                }
            }

            MetricsData(
                id = resId,
                name = "${m1.name} * ${m2.name}",
                rows = m1.rows,
                columns = m2.columns,
                data = resultData.map { it.toList() }
            )
        }
    }

    suspend fun transpose(id: String, resultId: String?): Result<MetricsData> {
        return try {
            val metric = metrics[id] ?: return Result.failure(NoSuchElementException("metric not found: $id"))
            val resId = resultId ?: "${id}_transpose"

            val duration = measureTimeMillis {
                val resultData = Array(metric.columns) { Array(metric.rows) { 0.0 } }

                for (i in metric.data.indices) {
                    for (j in metric.data[i].indices) {
                        resultData[j][i] = metric.data[i][j]
                    }
                }

                val result = MetricsData(
                    id = resId,
                    name = "${metric.name} Transposed",
                    rows = metric.columns,
                    columns = metric.rows,
                    data = resultData.map { it.toList() }
                )

                metrics[resId] = result
            }

            metricsCollector?.recordOperation("transpose", duration)
            val result = metrics[resId]!!
            Result.success(result)

        } catch (e: Exception) {
            logger.error("Failed to transpose metric $id", e)
            Result.failure(e)
        }
    }

    suspend fun calculateStatistics(id: String): Result<MetricsStatistics> {
        return try {
            val metric = metrics[id] ?: return Result.failure(NoSuchElementException("metric not found: $id"))

            val duration = measureTimeMillis {
                val flatData = metric.data.flatten()

                val mean = flatData.average()
                val variance = flatData.map { (it - mean) * (it - mean) }.average()
                val standardDeviation = sqrt(variance)
                val min = flatData.minOrNull() ?: 0.0
                val max = flatData.maxOrNull() ?: 0.0
                val sum = flatData.sum()

                val stats = MetricsStatistics(
                    metricsId = id,
                    mean = mean,
                    standardDeviation = standardDeviation,
                    variance = variance,
                    min = min,
                    max = max,
                    sum = sum
                )

                // Record statistics in metrics
                metricsCollector?.recordMetricsStats(id, stats)
            }

            metricsCollector?.recordOperation("statistics", duration)

            val flatData = metric.data.flatten()
            val mean = flatData.average()
            val variance = flatData.map { (it - mean) * (it - mean) }.average()

            Result.success(MetricsStatistics(
                metricsId = id,
                mean = mean,
                standardDeviation = sqrt(variance),
                variance = variance,
                min = flatData.minOrNull() ?: 0.0,
                max = flatData.maxOrNull() ?: 0.0,
                sum = flatData.sum()
            ))

        } catch (e: Exception) {
            logger.error("Failed to calculate statistics for metric $id", e)
            Result.failure(e)
        }
    }

    private suspend fun performBinaryOperation(
        operationType: String,
        id1: String,
        id2: String,
        resultId: String?,
        operation: (MetricsData, MetricsData, String) -> MetricsData
    ): Result<MetricsData> {
        return try {
            val metric1 = metrics[id1] ?: return Result.failure(NoSuchElementException("metric not found: $id1"))
            val metric2 = metrics[id2] ?: return Result.failure(NoSuchElementException("metric not found: $id2"))
            val resId = resultId ?: "${id1}_${operationType}_${id2}"

            val duration = measureTimeMillis {
                val result = operation(metric1, metric2, resId)
                metrics[resId] = result
            }

            metricsCollector?.recordOperation(operationType, duration)
            val result = metrics[resId]!!
            Result.success(result)

        } catch (e: Exception) {
            logger.error("Failed to perform $operationType on metrics $id1 and $id2", e)
            Result.failure(e)
        }
    }

    fun close() {
        metrics.clear()
        logger.info("metric engine closed")
    }
}