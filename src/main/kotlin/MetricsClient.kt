package com.example.metrics

import com.example.metrics.common.SystemCode
import com.example.metrics.logging.Logger
import com.example.metrics.model.Metric
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MetricsClient(
    private val client: HttpClient,
    private val logger: Logger,
    private val metricServerUrl: String = "http://127.0.0.1:8080/metrics"
) {
    suspend fun send(domain: String, method: String, userId: String) {
        val metric = Metric(domain, method, userId, null)
        val json = Json.encodeToString(metric)
        try {
            metric.validateFields()
            client.post(metricServerUrl) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
            logger.info("✅ Metric 전송 성공: $json")
        } catch (e: IllegalArgumentException) {
            logger.error("Metric 전송 실패 - 필드 검증 오류: ${SystemCode.FAILED}")
        } catch (e: CancellationException) {
            logger.warn("Metric 전송 취소됨: ${SystemCode.CANCELED.getMessage()}")
            throw e // 코루틴 취소는 다시 던져야 함
        } catch (e: Exception) {
            logger.info("Metric 전송 실패: ${SystemCode.ERROR}")
        }
    }
}