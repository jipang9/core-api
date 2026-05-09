package mobility.core.outbox.presentation

import mobility.core.reservation.application.ReservationCreateService
import mobility.core.reservation.presentation.dto.ReservationCreateRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationCreateService: ReservationCreateService
) {

    @PostMapping("/v1")
    fun create(
        @RequestBody request: ReservationCreateRequest
    ): ResponseEntity<Long> {

        val reservationId = reservationCreateService.execute(request)

        return ResponseEntity.ok(reservationId)
    }
}