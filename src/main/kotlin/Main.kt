package com.example.metrics

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("🚀 Metrics SDK 테스트 시작")

    MetricsClient.send("order", "create")
    MetricsClient.send("user", "login")
}