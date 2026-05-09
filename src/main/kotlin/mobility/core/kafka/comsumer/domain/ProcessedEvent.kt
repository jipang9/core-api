package mobility.core.kafka.comsumer.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "processed_events")
class ProcessedEvent(
    @Id
    val eventId: String,

    val processedAt: LocalDateTime = LocalDateTime.now()
)
