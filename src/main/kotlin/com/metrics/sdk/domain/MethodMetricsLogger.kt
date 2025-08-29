package com.metrics.sdk.domain

import com.metrics.sdk.model.MethodMetric
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

class MethodMetricsLogger {

    private val logger = LoggerFactory.getLogger(MethodMetricsLogger::class.java)
    private val metrics = ConcurrentLinkedQueue<MethodMetric>()
    private val totalCalls = AtomicLong(0)
    private val successfulCalls = AtomicLong(0)
    private val failedCalls = AtomicLong(0)
    private val json = Json { prettyPrint = true }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var exportJob: Job? = null

    fun startExportJob(intervalMs: Long = 60000) {
        exportJob?.cancel()
        exportJob = scope.launch {
            while (isActive) {
                delay(intervalMs)
                exportMetrics()
            }
        }
    }

    fun stopExportJob() {
        exportJob?.cancel()
        exportJob = null
    }

    fun recordMethodStart(methodName: String, parameters: Map<String, String>, domainId: String? = null): MethodMetric {
        val metric = MethodMetric(
            methodName = methodName,
            parameters = parameters,
            startTime = System.currentTimeMillis(),
            domainId = domainId
        )
        return metric
    }

    fun recordMethodEnd(metric: MethodMetric, successful: Boolean, errorMessage: String? = null): MethodMetric {
        val endTime = System.currentTimeMillis()
        val updatedMetric = metric.copy(
            endTime = endTime,
            executionTimeMs = endTime - metric.startTime,
            successful = successful,
            errorMessage = errorMessage
        )

        totalCalls.incrementAndGet()
        if (successful) successfulCalls.incrementAndGet() else failedCalls.incrementAndGet()

        metrics.add(updatedMetric)
        logger.debug("Method executed: ${updatedMetric.methodName}, time: ${updatedMetric.executionTimeMs}ms, success: $successful")

        return updatedMetric
    }

    fun getMetrics(): List<MethodMetric> = metrics.toList()

    fun clearMetrics() {
        metrics.clear()
        totalCalls.set(0)
        successfulCalls.set(0)
        failedCalls.set(0)
    }

    private fun exportMetrics() {
        if (metrics.isEmpty()) return

        val currentMetrics = metrics.toList()
        logger.info("Exporting ${currentMetrics.size} method metrics")

        // TODO: 실제 도메인 연동 시 DB나 외부 시스템으로 내보내기
        // 현재는 로그로만 출력
        logger.info("Method metrics summary: total=${totalCalls.get()}, success=${successfulCalls.get()}, failed=${failedCalls.get()}")

        // 샘플링으로 일부만 상세 로그
        if (currentMetrics.size > 5) {
            logger.debug("Sample metrics: ${json.encodeToString(currentMetrics.take(5))}")
        } else {
            logger.debug("All metrics: ${json.encodeToString(currentMetrics)}")
        }
    }

    // 향후 DB 저장을 위한 메서드
    suspend fun saveToDatabase(connectionString: String) {
        // DB 저장 로직 구현
        logger.info("Database export functionality will be implemented here")
    }

    // Grafana 대시보드용 JSON 생성
    fun generateGrafanaJson(): String {
        // Grafana 대시보드 형식에 맞는 JSON 생성
        return json.encodeToString(getMetricsStats())
    }

    private fun getMetricsStats(): Map<String, Any> {
        val allMetrics = metrics.toList()
        val methodCounts = allMetrics.groupBy { it.methodName }
            .mapValues { it.value.size }

        val avgExecutionTime = if (allMetrics.isNotEmpty()) {
            allMetrics.sumOf { it.executionTimeMs } / allMetrics.size.toDouble()
        } else 0.0

        val maxExecutionTime = allMetrics.maxOfOrNull { it.executionTimeMs } ?: 0

        return mapOf(
            "totalCalls" to totalCalls.get(),
            "successfulCalls" to successfulCalls.get(),
            "failedCalls" to failedCalls.get(),
            "averageExecutionTimeMs" to avgExecutionTime,
            "maxExecutionTimeMs" to maxExecutionTime,
            "methodCounts" to methodCounts
        )
    }
}