package mobility.core.outbox.application

import com.fasterxml.jackson.databind.ObjectMapper
import mobility.core.outbox.domain.OutboxEvent
import mobility.core.outbox.domain.enums.AggregateType
import mobility.core.outbox.domain.enums.EventType
import mobility.core.outbox.repository.OutboxEventRepository
import mobility.core.reservation.event.ReservationCreatedEvent
import org.springframework.stereotype.Component

@Component
class OutboxEventAppender(
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper
) {

    fun appendReservationCreated(
        reservationId: Long,
        carId: Long,
        userId: Long
    ) {

        val event = ReservationCreatedEvent(
            reservationId = reservationId,
            carId = carId,
            userId = userId
        )

        val outboxEvent = OutboxEvent(
            aggregateType = AggregateType.RESERVATION,
            aggregateId = reservationId.toString(),
            eventType = EventType.RESERVATION_CREATED,
            payload = objectMapper.writeValueAsString(event)
        )

        outboxEventRepository.save(outboxEvent)
    }
}