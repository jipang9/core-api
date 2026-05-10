package mobility.core.outbox.application

import mobility.core.outbox.domain.OutboxEvent
import mobility.core.outbox.domain.enums.OutboxStatus
import mobility.core.outbox.repository.OutboxEventRepository
import org.springframework.stereotype.Service

@Service
class OutboxEventFinder (
    private val outboxEventRepository: OutboxEventRepository
){

    fun getEventsBy(status: OutboxStatus): List<OutboxEvent> {
        return  outboxEventRepository.findAllByStatus(OutboxStatus.PENDING)
    }
}