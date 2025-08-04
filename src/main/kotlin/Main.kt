package com.example.metrics

import com.example.metrics.logging.Logger
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("🚀 Metrics SDK 테스트 시작")

    // HttpClient와 Logger를 생성
    val httpClient = HttpClient(CIO)
    val logger = object : Logger {
        override fun info(message: String) = println("INFO: $message")
        override fun warn(message: String) = println("WARN: $message")
    }

    // MetricsClient 인스턴스 생성
    val metricsClient = MetricsClient(httpClient, logger)

    // MetricsClient 사용
    metricsClient.healthCheck()
    metricsClient.send("user", "login", "user12222")
}