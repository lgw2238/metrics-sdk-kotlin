# Metrics SDK

## 📖 개요
Metrics SDK는 행렬 연산 및 메트릭 수집을 지원하는 Kotlin 라이브러리입니다.  
행렬 데이터를 생성, 연산, 통계 분석하며 Prometheus를 통해 메트릭을 모니터링할 수 있습니다.

---

## 🛠️ 주요 기능
- **행렬 생성 및 관리**: 행렬 데이터를 생성하고 저장.
- **행렬 연산**: 덧셈, 뺄셈, 곱셈, 전치 연산 지원.
- **통계 계산**: 평균, 분산, 표준편차 등 통계 정보 제공.
- **메트릭 모니터링**: Prometheus를 통해 실시간 메트릭 수집 및 노출.
- **비동기 처리**: Kotlin Coroutines를 활용한 비동기 연산.

---

## 📋 프로젝트 스펙
- **언어**: Kotlin
- **빌드 도구**: Gradle
- **주요 라이브러리**:
  - `kotlinx.serialization`: JSON 직렬화/역직렬화
  - `ktor-client-cio`: HTTP 클라이언트
  - `micrometer`: Prometheus 메트릭 수집
  - `coroutines`: 비동기 처리
- **기본 서버 포트**:
  - HTTP 서버: `8080`
  - Prometheus: `8081`
---

## 🚀 사용 방법
### 1. SDK 초기화
```kotlin
val sdk = MetricsSDK.create {
    enableMetrics(true)
    prometheusPort(8081)
}
```

### 2. 행렬 생성
```kotlin
val metrics = MetricsData(
    id = "metrics1",
    name = "Sample Metrics",
    rows = 2,
    columns = 2,
    data = listOf(
        listOf(1.0, 2.0),
        listOf(3.0, 4.0)
    )
)
sdk.createMatrix(metrics)
```

### 3. 행렬 연산
```kotlin
val result = sdk.add("metrics1", "metrics2").getOrThrow()
println("Addition Result: ${result.data}")
```

### 4. 통계 계산
```kotlin
val stats = sdk.calculateStatistics("metrics1").getOrThrow()
println("Metrics Statistics: $stats")
```

### 5. 메트릭 확인
Prometheus 서버에서 메트릭 데이터를 확인할 수 있습니다.  
기본 URL: `http://localhost:8081/metrics`

---

## 📂 디렉토리 구조
```text
src/
├── main/
│   ├── kotlin/
│   │   ├── com.metrics.sdk/
│   │   │   ├── Application.kt       // SDK 실행 테스트 (Ktor Application)
│   │   │   ├── MetricsSDK.kt        // SDK 진입점
│   │   │   ├── core/                // Metrics 매서드 
│   │   │   ├── model/               // 데이터 모델
│   │   │   ├── monitoring/          // Metrics 수집 로직
│   └── resources/
```

## 기본 설정 (domain version)
```kotlin
// 메트릭스 로거 및 파이프라인 초기화
val methodMetricsLogger = MethodMetricsLogger()
val metricsPipeline = MethodMetricsPipeline(methodMetricsLogger)

// 주기적인 메트릭스 내보내기 설정 (60초마다)
methodMetricsLogger.startExportJob(intervalMs = 60000)
```

## 사용 예제 코드 (매서드 메트릭스 로깅 - domain)
```Kotlin
fun testMethod(orderId: String, amount: Double): Boolean {
  // 메트릭스 수집 시작
  val context = metricsPipeline.startRecording(
    methodName = "testMethod",
    parameters = mapOf(
      "testId" to testId,
      "testMessage" to message.toString()
    ),
    domainId = "testSevice"
  )

  return try {
    // 실제 비즈니스 로직
    val result = testLogic.process(orderId, amount)

    // 성공 시 메트릭스 기록 완료
    context.end(successful = true)
    result
  } catch (e: Exception) {
    // 실패 시 메트릭스 기록 완료 (오류 메시지 포함)
    context.end(successful = false, errorMessage = e.message)
    throw e
  }
}
```
## 수집된 데이터 조회 (prometheus - domain)
```Kotlin
// 수집된 모든 메트릭스 조회
val metrics = methodMetricsLogger.getMetrics()

// 도메인별 성능 통계
val domainGroups = metrics.groupBy { it.domainId ?: "unknown" }
domainGroups.forEach { (domain, domainMetrics) ->
    println("[$domain] 도메인 통계:")
    val avgTime = domainMetrics.map { it.executionTimeMs }.average()
    val successRate = domainMetrics.count { it.successful } * 100.0 / domainMetrics.size
    println("  - 총 호출: ${domainMetrics.size}회")
    println("  - 평균 실행시간: ${avgTime.toInt()}ms")
    println("  - 성공률: ${String.format("%.1f", successRate)}%")
}
```