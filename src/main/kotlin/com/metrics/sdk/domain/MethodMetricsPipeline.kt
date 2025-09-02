package com.metrics.sdk.domain


import com.metrics.sdk.model.MethodMetric
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory

/**
 * Domain service to handle method metrics recording and logging.
 */
class MethodMetricsPipeline(
    private val metricsLogger: MethodMetricsLogger,
) {
    private val logger = LoggerFactory.getLogger(MethodMetricsPipeline::class.java)

    suspend fun <T> recordMethodCall(
        methodName: String,
        parameters: Map<String, String>,
        domainId: String? = null,
        block: suspend () -> T,
    ): T {
        val metric = metricsLogger.recordMethodStart(methodName, parameters, domainId)

        return try {
            val result = withContext(Dispatchers.Default) {
                block()
            }
            metricsLogger.recordMethodEnd(metric, true)
            result
        } catch (e: Exception) {
            logger.error("Error executing method $methodName: ${e.message}", e)
            metricsLogger.recordMethodEnd(metric, false, e.message)
            throw e
        }
    }

    fun startRecording(
        methodName: String,
        parameters: Map<String, String>,
        domainId: String? = null,
    ): MethodRecordingContext {
        val metric = metricsLogger.recordMethodStart(methodName, parameters, domainId)
        return MethodRecordingContext(metricsLogger, metric)
    }

    class MethodRecordingContext(
        private val metricsLogger: MethodMetricsLogger,
        private val metric: MethodMetric,
    ) {
        fun end(successful: Boolean = true, errorMessage: String? = null) {
            metricsLogger.recordMethodEnd(metric, successful, errorMessage)
        }
    }
}