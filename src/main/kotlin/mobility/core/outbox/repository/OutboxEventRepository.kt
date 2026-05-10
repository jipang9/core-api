package mobility.core.outbox.repository

import mobility.core.outbox.domain.OutboxEvent
import mobility.core.outbox.domain.enums.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {

    fun findAllByStatus(status: OutboxStatus): List<OutboxEvent>

    fun findAllByStatusIn(statuses:List<OutboxStatus>): List<OutboxEvent>
    
}