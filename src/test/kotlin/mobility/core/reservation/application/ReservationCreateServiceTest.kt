package mobility.core.reservation.application

import mobility.core.outbox.domain.enums.AggregateType
import mobility.core.outbox.domain.enums.OutboxStatus
import mobility.core.outbox.repository.OutboxEventRepository
import mobility.core.reservation.presentation.dto.ReservationCreateRequest
import mobility.core.reservation.repository.ReservationRepository
import mobility.core.support.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReservationCreateServiceTest(

    @Autowired
    private val reservationCreateService: ReservationCreateService,

    @Autowired
    private val reservationRepository: ReservationRepository,

    @Autowired
    private val outboxEventRepository: OutboxEventRepository

) : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {

        outboxEventRepository.deleteAll()
        reservationRepository.deleteAll()
    }

    @Test
    fun `예약 생성 시 Outbox 이벤트도 함께 저장된다`() {

        // given
        val request = ReservationCreateRequest(
            carId = 1L,
            userId = 100L
        )

        // when
        val reservationId =
            reservationCreateService.execute(request)

        // then
        val reservation =
            reservationRepository.findById(reservationId)

        assertThat(reservation).isPresent

        val outboxEvents =
            outboxEventRepository.findAll()

        assertThat(outboxEvents).hasSize(1)

        val outboxEvent = outboxEvents.first()

        assertThat(outboxEvent.aggregateType)
            .isEqualTo(AggregateType.RESERVATION)

        assertThat(outboxEvent.aggregateId)
            .isEqualTo(reservationId.toString())

        assertThat(outboxEvent.status)
            .isEqualTo(OutboxStatus.PENDING)
    }
}