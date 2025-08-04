package com.example.metrics

import com.example.metrics.common.SystemCode
import com.example.metrics.logging.Logger
import com.example.metrics.model.Metric
import io.ktor.client.*
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
    /**
     * Health Check 메서드
     * 서버의 상태를 확인하기 위해 사용됩니다.
     */
    suspend fun healthCheck() {
        val healthCheckUrl = "http://127.0.0.1:8080/healthCheck"
        try {
            client.post(healthCheckUrl) {
                contentType(ContentType.Application.Json)
                setBody("")
            }
            logger.info("✅ Metric 전송 성공: Health-Check")
        } catch (e: IllegalArgumentException) {
            logger.error("Metric 전송 실패 Health-Check 오류: ${SystemCode.FAILED} , ${e.message}")
        } catch (e: CancellationException) {
            logger.warn("Metric Health-Check 전송 취소됨: ${SystemCode.CANCELED.getMessage()}")
            throw e // 코루틴 취소는 다시 던져야 함
        } catch (e: Exception) {
            logger.info("Metric Health-Check 전송 실패: ${SystemCode.ERROR}")
        }
    }

    /**
     * Metric 전송 메서드
     * @param domain 도메인 이름
     * @param method 메서드 이름
     * @param userId 사용자 ID
     * @throws IllegalArgumentException 메트릭 전송이 validation 의해 실패한 경우
     */
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
            logger.error("Metric 전송 실패 - 필드 검증 오류: ${SystemCode.FAILED} , ${e.message}")
        } catch (e: CancellationException) {
            logger.warn("Metric 전송 취소됨: ${SystemCode.CANCELED.getMessage()}")
            throw e // 코루틴 취소는 다시 던져야 함
        } catch (e: Exception) {
            logger.info("Metric 전송 실패: ${SystemCode.ERROR}")
        }
    }


}