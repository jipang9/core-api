# Mobility Core Platform

A mobility reservation platform built with Kotlin + Spring Boot.

This project focuses on solving real-world problems commonly found in mobility services:

- High-concurrency reservation requests
- Distributed locking strategies
- Event consistency
- Kafka message delivery guarantees
- Consumer idempotency
- Transactional Outbox Pattern

---

# Tech Stack

- Kotlin
- Spring Boot
- Spring Data JPA
- MySQL
- Redis
- Apache Kafka
- Docker Compose
- Testcontainers
- JUnit5
- k6

---

# Project Goals

This project was designed to simulate real-world mobility platform problems such as:

- duplicate reservation requests
- concurrent vehicle booking
- event consistency between DB and Kafka
- duplicate Kafka message consumption
- distributed locking under high TPS environments

The main purpose is to validate:
- reliability
- consistency
- scalability
- concurrency handling

under heavy traffic scenarios.

---

# Architecture

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

This project implements the Transactional Outbox Pattern
to guarantee event consistency between the database and Kafka.

## Problem

If a Reservation is saved successfully,
but Kafka publishing fails immediately after,
data inconsistency may occur.

Example:

- Reservation saved in DB
- Kafka event publish failed
- Downstream systems never receive the event

This creates inconsistency between services.

## Solution

Reservation data and OutboxEvent are stored
within the same database transaction.

```text
Transaction
 ├── Save Reservation
 └── Save OutboxEvent
```

After the transaction commits successfully,
a relay scheduler publishes pending OutboxEvents to Kafka.

--

# Kafka Delivery Guarantee

This project uses Kafka with an **At-Least-Once delivery** strategy.

At-Least-Once means Kafka guarantees
that messages are delivered at least once.

However, duplicate message consumption may occur.

---

# Why Duplicate Consumption Can Happen

A duplicate-consumption scenario may occur in the following case:

1. The Consumer receives a reservation event from Kafka.
2. The Consumer successfully processes business logic
   (e.g. SmartKey issue, reservation confirmation).
3. Before committing the Kafka offset,
   the Consumer crashes due to:
    - server shutdown
    - OOM
    - network failure
    - process kill
4. Kafka detects that the offset was not committed.
5. Kafka assumes the message was not processed.
6. Kafka re-delivers the same message.

As a result,
the same event may be processed multiple times.

---

# Consumer Idempotency Strategy

To safely handle duplicate messages,
this project implements Consumer idempotency
using a `processed_events` table.

Before processing an event:

1. Check whether `eventId` already exists
2. If already processed → ignore
3. Otherwise:
    - process business logic
    - store processed event history

This guarantees that duplicate Kafka messages
do not create duplicate side effects.

---

# Why Idempotency Matters in Mobility Services

Mobility platforms frequently handle
high-concurrency reservation requests.

Without idempotency handling,
duplicate Kafka messages may cause critical issues such as:

- duplicate vehicle reservations
- duplicate SmartKey issuance
- duplicate payment processing
- duplicate notification sending

To prevent these issues,
this project combines:

- Transactional Outbox Pattern
- Kafka At-Least-Once Delivery
- Consumer Idempotency

---

# Distributed Lock Strategy Comparison

This project compares multiple locking strategies
under high-concurrency reservation scenarios.

## Compared Strategies

- DB Pessimistic Lock
- ShedLock
- Redis Redlock (Redisson)

## Scenario

```text
Multiple users attempt to reserve
the same vehicle simultaneously.
```

The project measures:

- TPS
- average response time
- duplicate reservation count
- failure rate

using k6 load testing.

---

# Test Strategy

This project uses:

- JUnit5
- Testcontainers
- Integration Testing
- JaCoCo

to validate:

- Reservation + Outbox atomicity
- Kafka publish flow
- Consumer idempotency
- Distributed lock behavior

---

# Load Testing

k6 is used for concurrency and TPS testing.

Test scenarios include:

- 100 TPS
- 500 TPS
- 1000 TPS
- concurrent reservation requests

The goal is to analyze
locking strategy limitations and scalability.

---

# Local Development

## Start Infrastructure

```bash
docker compose up -d
```

## Run Application

```bash
./gradlew bootRun
```

## Run Tests

```bash
./gradlew test
```

---

# Future Improvements

- Kubernetes deployment
- Multi-module architecture
- Retry + DLQ strategy
- Kafka partition optimization
- Observability (Prometheus + Grafana)
- CQRS/Event Sourcing experimentation

---

# Lessons Learned

This project focuses not only on implementing Kafka,
but also on understanding:

- event consistency
- concurrency control
- idempotency
- distributed systems trade-offs
- real-world mobility platform problems