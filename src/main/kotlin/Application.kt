package com.metrics.sdk

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import com.metrics.sdk.model.MetricsData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    // SDK 인스턴스 생성 (프로메테우스 메트릭스 활성화)
    val sdk = MetricsSDK.create {
        enableMetrics(true)
        prometheusPort(8081)
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        routing {
            get("/metrics") {
                val snapshot = sdk.getMetricsSnapshot()
                println("Snapshot: $snapshot")
                snapshot?.let { call.respondText(it, ContentType.Text.Plain) }
            }
        }
    }.start(wait = true)

    runBlocking {
        // 테스트용 메트릭스 생성
        val metrics1 = MetricsData(
            id = "metrics1",
            name = "Test Metrics 1",
            rows = 2,
            columns = 2,
            data = listOf(
                listOf(1.0, 2.0),
                listOf(3.0, 4.0)
            )
        )

        val metrics2 = MetricsData(
            id = "metrics2",
            name = "Test Metrics 2",
            rows = 2,
            columns = 2,
            data = listOf(
                listOf(5.0, 6.0),
                listOf(7.0, 8.0)
            )
        )

        // 메트릭스 저장
        sdk.createMetrics(metrics1).getOrThrow()
        sdk.createMetrics(metrics2).getOrThrow()

        // 메트릭스 연산 테스트
        val addResult = sdk.add("metrics1", "metrics2").getOrThrow()
        println("Add result: ${addResult.data}")

        val multiplyResult = sdk.multiply("metrics1", "metrics2").getOrThrow()
        println("Multiply result: ${multiplyResult.data}")

        val transposeResult = sdk.transpose("metrics1").getOrThrow()
        println("Transpose result: ${transposeResult.data}")

        // 통계 계산
        val stats = sdk.calculateStatistics("metrics1").getOrThrow()
        println("Statistics: $stats")

        // 메트릭스 스냅샷 출력
        println("Metrics snapshot:")
        println(sdk.getMetricsSnapshot())
    }
}