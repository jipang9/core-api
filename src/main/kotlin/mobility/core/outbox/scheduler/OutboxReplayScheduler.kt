package mobility.core.outbox.scheduler

import mobility.core.outbox.application.OutboxRelayService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxReplayScheduler(
    private val outboxRelayService: OutboxRelayService
) {

    @Scheduled(
        fixedDelayString = "\${outbox.polling.fixed-delay}"
    )
    fun relay() {
        outboxRelayService.publishPendingEvents();
    }
}
