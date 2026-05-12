# Mobility Core Platform

Kotlin + Spring Boot 기반의 모빌리티 예약 플랫폼 프로젝트입니다.

이 프로젝트는 실제 모빌리티 서비스에서 자주 발생하는 문제들을 해결하는 데 초점을 맞추고 있습니다.

* 대량 동시 예약 요청 처리
* 분산 락 전략 비교
* 이벤트 정합성(Event Consistency)
* Kafka 메시지 전달 보장
* Consumer 멱등성(Idempotency)
* Transactional Outbox Pattern

---

# 기술 스택

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

---

# 프로젝트 목표

이 프로젝트는 실제 모빌리티 플랫폼에서 발생할 수 있는 다음과 같은 문제 상황을 시뮬레이션하기 위해 설계되었습니다.

* 중복 예약 요청
* 동일 차량에 대한 동시 예약
* DB와 Kafka 간 이벤트 정합성 문제
* Kafka 메시지 중복 소비
* 높은 TPS 환경에서의 분산 락 처리

주요 검증 목적은 다음과 같습니다.

* 안정성(Reliability)
* 정합성(Consistency)
* 확장성(Scalability)
* 동시성 처리(Concurrency Handling)

---

# 예약 연동 플로우

```text
Client
  ↓
Reservation API
  ↓
ReservationService
  ↓
[Distributed Lock]
  ↓
MySQL Transaction
 ├── reservations
 └── outbox_events
  ↓
Outbox Relay Scheduler
  ↓
Kafka
  ↓
Kafka Consumer
  ↓
processed_events (Idempotency)
```

---

# Transactional Outbox Pattern

이 프로젝트는 DB와 Kafka 사이의 이벤트 정합성을 보장하기 위해
Transactional Outbox Pattern을 적용했습니다.

## 문제 상황

Reservation 저장은 성공했지만,
직후 Kafka publish가 실패할 경우 데이터 불일치가 발생할 수 있습니다.

예시:

* Reservation은 DB 저장 성공
* Kafka 이벤트 발행 실패
* 하위 서비스에서는 이벤트를 전달받지 못함

결과적으로 서비스 간 데이터 정합성이 깨지게 됩니다.

## 해결 방법

Reservation 데이터와 OutboxEvent를
동일한 DB 트랜잭션 내에서 함께 저장합니다.

```text
Transaction
 ├── Save Reservation
 └── Save OutboxEvent
```

트랜잭션이 정상 commit된 이후,
Scheduler가 PENDING 상태의 OutboxEvent를 Kafka로 발행합니다.

---

# Kafka 전달 보장 전략

이 프로젝트는 Kafka의 **At-Least-Once Delivery** 전략을 기반으로 구현되었습니다.

At-Least-Once는 메시지가 최소 한 번 이상 전달됨을 보장합니다.

하지만 이 과정에서 동일 메시지가 중복 소비될 가능성이 존재합니다.

---

# Kafka에서 중복 소비가 발생할 수 있는 이유

다음과 같은 상황에서 중복 소비가 발생할 수 있습니다.

1. Consumer가 Kafka 메시지를 수신
2. 비즈니스 로직 처리 성공
   (예: 스마트키 발급, 예약 확정)
3. Kafka offset commit 이전에 Consumer 장애 발생

   * 서버 종료
   * OOM
   * 네트워크 장애
   * 프로세스 강제 종료
4. Kafka는 offset commit이 되지 않은 것을 감지
5. Kafka는 해당 메시지가 처리되지 않았다고 판단
6. 동일 메시지를 다시 전달

결과적으로 동일 이벤트가 여러 번 처리될 수 있습니다.

---

# Consumer 멱등성(Idempotency) 처리 전략

중복 메시지를 안전하게 처리하기 위해
`processed_events` 테이블 기반의 Consumer 멱등성 처리를 구현했습니다.

이벤트 처리 전:

1. `eventId` 존재 여부 확인
2. 이미 처리된 이벤트면 무시
3. 처리되지 않은 이벤트라면:

   * 비즈니스 로직 수행
   * 처리 이력 저장

이를 통해 Kafka 메시지가 중복 전달되더라도
중복 부작용이 발생하지 않도록 보장합니다.

---

# 모빌리티 서비스에서 멱등성이 중요한 이유

모빌리티 플랫폼은 대량의 동시 예약 요청을 처리합니다.

멱등성 처리가 없다면,
중복 Kafka 메시지로 인해 다음과 같은 문제가 발생할 수 있습니다.

* 동일 차량 중복 예약
* 스마트키 중복 발급
* 결제 중복 처리
* 알림 중복 발송

이 프로젝트는 이러한 문제를 방지하기 위해 다음 구조를 함께 사용합니다.

* Transactional Outbox Pattern
* Kafka At-Least-Once Delivery
* Consumer Idempotency

---

# 분산 락 전략 비교

이 프로젝트는 동시 예약 환경에서
여러 분산 락 전략을 비교합니다.

## 비교 대상

* DB Pessimistic Lock
* ShedLock
* Redis Redlock (Redisson)

## 시나리오

```text
여러 사용자가 동일 차량을 동시에 예약 요청
```

다음 항목을 측정합니다.

* TPS
* 평균 응답 시간
* 중복 예약 발생 수
* 실패율

k6 기반 부하 테스트를 통해 측정합니다.

---

# 테스트 전략

다음 기술을 사용합니다.

* JUnit5
* Testcontainers
* Integration Testing
* JaCoCo

검증 대상:

* Reservation + Outbox 원자성
* Kafka publish 흐름
* Consumer 멱등성
* 분산 락 동작 검증

---

# 부하 테스트

k6를 이용해 TPS 및 동시성 테스트를 수행합니다.

테스트 시나리오:

* 100 TPS
* 500 TPS
* 1000 TPS
* 동시 예약 요청

목표는 락 전략의 한계와 확장성을 분석하는 것입니다.

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

# 향후 개선 예정

* Kubernetes 배포
* Multi-module 구조 전환
* Retry + DLQ 전략
* Kafka Partition 최적화
* Observability 구축 (Prometheus + Grafana)
* CQRS / Event Sourcing 실험

---

# 프로젝트를 통해 학습한 내용

이 프로젝트는 단순히 Kafka를 사용하는 것에 목적이 있지 않습니다.

다음과 같은 내용을 실제로 구현하고 이해하는 데 집중했습니다.

* 이벤트 정합성
* 동시성 제어
* 멱등성 처리
* 분산 시스템의 트레이드오프
* 실제 모빌리티 플랫폼 문제 해결 방식
