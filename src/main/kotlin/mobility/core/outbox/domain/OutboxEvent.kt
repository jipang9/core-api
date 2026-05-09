package mobility.core.outbox.domain

import jakarta.persistence.*
import mobility.core.outbox.domain.enums.OutboxStatus
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "outbox_events",
    indexes = [
        Index(name = "idx_outbox_status", columnList = "status"),
        Index(name = "idx_outbox_created_at", columnList = "createdAt")
    ]
)
class OutboxEvent (
    @Column(nullable = false, unique = true)
    val eventId: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val aggregateType: String,

    @Column(nullable = false)
    val aggregateId: String,

    @Column(nullable = false)
    val eventType: String,

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxStatus = OutboxStatus.PENDING,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var publishedAt: LocalDateTime? = null,

    // 사용처 : kafka 발행 실패 원인 추적
    @Column
    var failureReason: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {

    fun markPublished() {
        status = OutboxStatus.PUBLISHED
        publishedAt = LocalDateTime.now()
    }

    fun markFailed(reason: String) {
        status = OutboxStatus.FAILED
        failureReason = reason
    }
}