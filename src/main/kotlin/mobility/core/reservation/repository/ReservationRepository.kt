package mobility.core.reservation.repository

import mobility.core.reservation.domain.Reservation
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationRepository : JpaRepository<Reservation, Long> {

}