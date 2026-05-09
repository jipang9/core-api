package mobility.core.reservation.domain

import jakarta.persistence.*
import mobility.core.reservation.domain.enums.ReservationStatus
import java.time.LocalDateTime

@Entity
@Table(
    name = "reservations",
    indexes = [
        Index(name = "idx_car_id", columnList = "carId"),
        Index(name = "idx_user_id", columnList = "userId")
    ]
)
class Reservation(

    @Column(nullable = false)
    val carId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.CREATED,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var confirmedAt: LocalDateTime? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {

    fun confirm() {
        status = ReservationStatus.CONFIRMED
        confirmedAt = LocalDateTime.now()
    }

    fun cancel() {
        status = ReservationStatus.CANCELLED
    }

    fun complete() {
        status = ReservationStatus.COMPLETED
    }
}