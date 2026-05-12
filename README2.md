파일 저장 권한이 이번 세션에서 제한되어 있습니다. README 전체 내용을 바로 드릴 테니 복사해서 `README.md` 파일로 저장해주세요.

---

```markdown
# smart-outbox-kafka

Transactional Outbox Pattern + Apache Kafka 기반 이벤트 발행 보장 구현 예제

Kotlin · Spring Boot 3.2 · MySQL · Redis · Kafka

---

## 배경 / Background

실무에서 외부 콜백 지연(CallBack Delay) 문제를 해결하기 위해
**Spring 내부 이벤트 + Outbox Pattern**을 도입한 경험이 있습니다.
단일 서비스 내에서는 효과적이었지만, MSA 환경에서 서비스 간 메시지를
신뢰성 있게 전달하려면 메시지 브로커와의 통합이 필요합니다.

이 프로젝트는 다음 질문에 답합니다.

> "분산 서비스 환경에서 DB 트랜잭션과 Kafka 발행을 어떻게 원자적으로 보장할 수 있을까?"

**핵심 목표 세 가지:**

1. **At-Least-Once 발행 보장** — 서비스 재시작·Kafka 장애 상황에서도 이벤트 유실 없음
2. **Consumer 멱등성 처리** — 중복 수신 시 중복 처리 방지
3. **k6 부하 테스트 수치 확보** — 실측 TPS로 이력서·면접 근거 마련

---

## 아키텍처

```
┌────────────────────────────────────────────┐
│              order-service                  │
│                                             │
│  POST /api/orders                           │
│       │                                     │
│       └── @Transactional ──────────────┐   │
│              │                         │   │
│        orders 테이블 INSERT             │   │
│        outbox_events 테이블 INSERT      │   │
│        (status = PENDING)              │   │
│              └──────────── COMMIT ─────┘   │
│                                             │
│  @Scheduled(fixedDelay = 1000ms)            │
│       │                                     │
│       ├── SELECT ... WHERE status=PENDING   │
│       ├── KafkaProducer.send(event)         │
│       └── UPDATE status = PUBLISHED         │
└────────────────────────────────────────────┘
│
[Kafka Topic]
order-events
│
┌────────────────────────────────────────────┐
│             delivery-service                │
│                                             │
│  @KafkaListener("order-events")             │
│       │                                     │
│       ├── processed_events 테이블 조회       │
│       │     이미 처리됨 → skip (멱등성)      │
│       │     미처리  → 배달 로직 수행          │
│       └── processed_events INSERT           │
└────────────────────────────────────────────┘
```

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

## 프로젝트 구조

```
smart-outbox-kafka/
├── docker-compose.yml
├── order-service/                  # 주문 서비스 (Kotlin Spring Boot)
│   └── src/main/kotlin/
│       ├── order/
│       │   ├── OrderController.kt
│       │   ├── OrderService.kt
│       │   └── OrderRepository.kt
│       ├── outbox/
│       │   ├── OutboxEvent.kt      # @Entity — outbox_events 테이블
│       │   ├── OutboxRepository.kt
│       │   └── OutboxRelay.kt      # @Scheduled 폴링 → Kafka 발행
│       └── kafka/
│           └── KafkaProducerConfig.kt
├── delivery-service/               # 배달 서비스 (Consumer + 멱등성)
│   └── src/main/kotlin/
│       ├── delivery/
│       │   └── DeliveryService.kt
│       └── kafka/
│           ├── OrderEventConsumer.kt
│           └── ProcessedEventRepository.kt
└── load-test/
    └── order.js                    # k6 부하 테스트 스크립트
```

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Kotlin 1.9 / JVM 17 |
| Framework | Spring Boot 3.2 |
| Messaging | Apache Kafka (Confluent 7.5) |
| Database | MySQL 8.0 + Spring Data JPA |
| Cache | Redis 7.2 |
| Load Test | k6 |
| Infra | Docker Compose |

---

## 빠른 시작

#