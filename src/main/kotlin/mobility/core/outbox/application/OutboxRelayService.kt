package mobility.core.outbox.application

import mobility.core.kafka.producer.ReservationEventProducer
import mobility.core.outbox.domain.enums.OutboxStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxRelayService(
    private val outboxEventFinder: OutboxEventFinder,
    private val reservationEventProducer: ReservationEventProducer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun publishPendingEvents() {
        var pendingEvents = outboxEventFinder.getEventsBy(OutboxStatus.PENDING)

        pendingEvents.forEach { event ->
            try {
                reservationEventProducer.publish(
                    topic = "reservation.created",
                    key = event.aggregateId,
                    payload = event.payload
                )

                event.markPublished()

                log.info(
                    "Outbox event published. eventId={}",
                    event.eventId
                )
            } catch (e: Exception) {
                event.markFailed(e.message ?: "unknown error")

                log.error(
                    "Failed to publish outbox event. eventId={}",
                    event.eventId,
                    e
                )
            }
        }
    }
}