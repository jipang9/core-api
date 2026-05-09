package mobility.core.reservation.presentation.dto

data class ReservationCreateRequest (
    val carId: Long,
    var userId: Long
)