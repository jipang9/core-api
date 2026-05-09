package mobility.core.reservation.application

import jakarta.transaction.Transactional
import mobility.core.outbox.application.OutboxEventAppender
import mobility.core.reservation.domain.Reservation
import mobility.core.reservation.presentation.dto.ReservationCreateRequest
import mobility.core.reservation.repository.ReservationRepository
import org.springframework.stereotype.Service

@Service
class ReservationCreateService(
    private val reservationRepository: ReservationRepository,
    private val outboxEventAppender: OutboxEventAppender,
) {

    @Transactional
    fun execute(createRequest: ReservationCreateRequest): Long {

        val reservation = reservationRepository.save(
            Reservation(
                carId = createRequest.carId,
                userId = createRequest.userId
            )
        )

        val reservationId = reservation.id!!

        outboxEventAppender.appendReservationCreated(
            reservationId = reservationId,
            carId = reservation.carId,
            userId = reservation.userId
        )

        return reservationId
    }
}