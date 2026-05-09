package mobility.core.reservation.event

class ReservationCreatedEvent (
    var reservationId: Long,
    val carId: Long,
    val userId: Long
)