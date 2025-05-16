# Metrics SDK

## 📖 개요
Metrics SDK는 애플리케이션에서 발생하는 이벤트 데이터를 중개 서버로 전송하기 위한 간단한 라이브러리입니다. 
이 SDK는 이벤트 데이터를 JSON 형식으로 직렬화하고, HTTP POST 요청을 통해 지정된 서버로 전송합니다.
- 해당 라이브러리는 onPromise 환경을 스탠다드로 설계되어, 추후 queue 기반의 적재 매커니즘을 추가할 예정입니다.
---

## 🛠️ 주요 기능
- **이벤트 데이터 전송**: `Metric` 객체를 생성하여 중개 서버로 전송.
- **자동 타임스탬프 설정**: `timestamp` 값을 명시하지 않으면 자동으로 현재 시간을 설정.
- **HTTP 통신**: Ktor HTTP 클라이언트를 사용하여 비동기 요청 처리.
- **JSON 직렬화**: `kotlinx.serialization`을 사용하여 데이터를 JSON 형식으로 변환.

---

## 📋 스펙
- **언어**: Kotlin
- **빌드 도구**: Gradle
- **의존성**:
  - `kotlinx.serialization`: JSON 직렬화/역직렬화
  - `ktor-client-cio`: HTTP 클라이언트
- **기본 서버 URL**: `http://127.0.0.1:8080/metrics`

---

## 디렉토리 구조
```Text
src/
├── main/
│   ├── kotlin/
│   │   ├── com.example.metrics/
│   │   │   ├── Main.kt          // SDK 테스트 실행
│   │   │   ├── Metric.kt        // Metric 데이터 클래스
│   │   │   ├── MetricsClient.kt // Metric 전송 로직
│   └── resources/
```
