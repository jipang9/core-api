# Mobility Core Platform
## 스마트키 시스템을 더욱 강력하고 견고하게 설계해보자
이 프로젝트는 실제 모빌리티 서비스를 개발하며 직면한 문제를 분석하고 기술적 의사결정을 통해 개선하는데 초점을 맞추고 있습니다.
직면한 문제 뿐만 아니라 문제 상황을 시뮬레이션하고 해결하기 위해 설계되었습니다.

### 사용 기술 스택

* Kotlin
* Spring Boot
* Spring Data JPA
* MySQL
* Redis
* Apache Kafka
* Docker Compose
* Testcontainers
* JUnit5
* k6

### 문제 탐색 및 키워드
* 대량 동시 예약 요청 처리
* 멀티 인스턴스에서 분산 락 전략 비교
* 이벤트 정합성(Event Consistency)
* Kafka 메시지 전달 보장
* Consumer 멱등성(Idempotency)
* Transaction Outbox Pattern

---

## 배경 / Background

실무에서 외부 응답 콜백 지연(CallBack Delay) 문제를 해결하기 위해
**Spring 내부 이벤트(Spring Application Event) + Outbox Pattern** 구조를 설계 및 도입했습니다.
당시 출고 시나리오에 차량 상태 갱신 및 최신 차량 데이터를 사용하기 위해 외부 API를 호출하고 있었습니다.

다만 외부 시스템 응답이 즉각적인 응답이 아닌 CallBack 형태로 전달되다 보니 아래와 같은 문제가 발생했고, 이는 차량 출고 실패로 연결되었습니다.
* 출고 요청 실패
* 외부 응답 도착 전까지 장시간 대기
* 특정 외부 시스템 지연으로 전체 출고 흐름 영향
* 실시간 차량 상태 동기화 실패

출고의 책임을 갖는 API 입장에선 외부 API의 호출 자체보다 최신 차량 데이터 상태를 안정적으로 사용하는 것이 핵심 요구사항이었습니다.

로직 내 동기 처리의 흐름을 이벤트와 Outbox Pattern 을 통해 개선하여 출고율을 개선했지만
다만 이후 시스템 규모와 트래픽이 증가하면서,
DB Polling 기반 Outbox 구조의 한계도 경험하게 되었습니다.

예를 들어:

* Polling 주기에 따른 실시간성 한계
* 지속적인 DB polling 부하 증가
* 이벤트 처리량 증가 시 DB 의존도 상승
* 다중 인스턴스 환경에서 polling 경쟁 고려 필요

등의 문제가 존재했습니다.

이 경험을 계기로:

Kafka 기반 메시지 브로커 구조
At-Least-Once Delivery
Consumer Idempotency
Retry / DLQ 전략
Event-Driven Architecture

에 관심을 가지게 되었고,

현재는 Kotlin + Spring Boot 기반 개인 프로젝트에서
Kafka 기반 Transactional Outbox Pattern을 직접 구현하며
실제 운영 환경에서 발생할 수 있는 정합성 및 동시성 문제를 실험하고 있습니다.



> "분산 서비스 환경에서 DB 트랜잭션과 Kafka 발행을 어떻게 원자적으로 보장할 수 있을까?"

**핵심 목표 세 가지:**

1. **At-Least-Once 발행 보장** — 서비스 재시작·Kafka 장애 상황에서도 이벤트 유실 없음
2. **Consumer 멱등성 처리** — 중복 수신 시 중복 처리 방지
3. **k6 부하 테스트 수치 확보** — 실측 TPS로 이력서·면접 근거 마련
---


## 왜 Outbox Pattern인가

### 문제: 이중 쓰기(Dual Write)의 위험

```kotlin
// 위험한 패턴 — DB 저장 후 Kafka 발행 실패 시 이벤트 유실
fun placeOrder(request: OrderRequest) {
    orderRepository.save(order)                   // DB 저장 성공
    kafkaTemplate.send("order-events", event)     // ← 여기서 실패하면?
}
```

### 해결: Outbox Pattern

```kotlin
// 안전한 패턴 — 동일 트랜잭션에서 주문과 이벤트를 함께 저장
@Transactional
fun placeOrder(request: OrderRequest) {
    orderRepository.save(order)
    outboxRepository.save(
        OutboxEvent(aggregateId = order.id, payload = toJson(event))
    )
    // 두 INSERT가 하나의 트랜잭션 → 원자적 보장
}

// 별도 스케줄러가 주기적으로 발행
@Scheduled(fixedDelay = 1000)
fun publishPendingEvents() {
    outboxRepository.findByStatus(PENDING).forEach { event ->
        kafkaTemplate.send("order-events", event.payload)
        event.status = PUBLISHED
        outboxRepository.save(event)
    }
}
```

---


# 로컬 실행 방법

## 인프라 실행

```bash
docker compose up -d
```

## 애플리케이션 실행

```bash
./gradlew bootRun
```

## 테스트 실행

```bash
./gradlew test
```

---
