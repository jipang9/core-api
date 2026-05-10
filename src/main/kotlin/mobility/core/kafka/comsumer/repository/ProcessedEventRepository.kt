package mobility.core.kafka.comsumer.repository

import mobility.core.kafka.comsumer.domain.ProcessedEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedEventRepository : JpaRepository<ProcessedEvent, String>