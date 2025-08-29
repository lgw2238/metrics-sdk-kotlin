package com.metrics.sdk

import com.metrics.sdk.domain.MethodMetricsLogger
import com.metrics.sdk.domain.MethodMetricsPipeline
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val logger = MethodMetricsLogger()
    val pipeline = MethodMetricsPipeline(logger)

    // 자동 메트릭스 수집
    pipeline.recordMethodCall(
        methodName = "exampleMethod",
        parameters = mapOf("param1" to "value1", "param2" to "123"),
        domainId = "userService"
    ) {
        // 실제 메서드 로직
        println("Executing example method")
        Thread.sleep(100) // 작업 시뮬레이션
    }

    // 수동 메트릭스 수집
    val context = pipeline.startRecording(
        methodName = "manualMethod",
        parameters = mapOf("id" to "user123"),
        domainId = "authService"
    )

    try {
        // 실제 메서드 로직
        println("Executing manual method")
        Thread.sleep(150)
        context.end(true)
    } catch (e: Exception) {
        context.end(false, e.message)
    }

    // 결과 확인
    val metrics = logger.getMetrics()
    println("Collected metrics: ${metrics.size}")
    metrics.forEach { println(it) }
}