package com.example.metrics

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MetricsClient {
    private val client = HttpClient(CIO)
    // Handling Server URL
    private const val METRIC_SERVER_URL = "http://127.0.0.1:8080/metrics"

    /**
     * why suspend?
     * Apply coroutine in case there are more simultaneous requests
     */
    suspend fun send(domain: String, method: String) {
        val metric = Metric(domain, method, 1.toString(), null)
        val json = Json.encodeToString(metric)

        try {
            client.post(METRIC_SERVER_URL) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
            println("✅ Metric 전송 성공: $json")
        } catch (e: Exception) {
            println("❌ Metric 전송 실패: ${e.message}")
        }
    }
}