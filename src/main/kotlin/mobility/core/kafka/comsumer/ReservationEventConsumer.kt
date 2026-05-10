package mobility.core.kafka.comsumer

import com.fasterxml.jackson.databind.ObjectMapper
import mobility.core.kafka.comsumer.domain.ProcessedEvent
import mobility.core.kafka.comsumer.repository.ProcessedEventRepository
import mobility.core.kafka.config.KafkaConsumerGroups
import mobility.core.kafka.config.KafkaTopics
import mobility.core.reservation.event.ReservationCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReservationEventConsumer (
    private val objectMapper: ObjectMapper,
    private val processedEventRepository: ProcessedEventRepository
){
    private val log = LoggerFactory.getLogger(javaClass)
    @KafkaListener(
        topics = [KafkaTopics.RESERVATION_CREATED],
        groupId = KafkaConsumerGroups.RESERVATION_CONSUMER_GROUP
    )
    @Transactional
    fun consume(message: String){

        log.info("Kafka message received. payload={}", message)

        val event = objectMapper.readValue(
            message, ReservationCreatedEvent::class.java
        )

        val eventId = event.reservationId.toString();

        val alreadyProcessed =
            processedEventRepository.existsById(eventId)

        if (alreadyProcessed) {

            log.info(
                "Duplicate event ignored. eventId={}",
                eventId
            )

            return
        }

        log.info(
            "Process reservation event. reservationId={}",
            event.reservationId
        )

        processedEventRepository.save(
            ProcessedEvent(eventId = eventId)
        )
    }
}