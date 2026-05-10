package mobility.core.kafka.producer

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ReservationEventProducer(

    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(
        topic: String,
        key: String,
        payload: String
    ) {
        kafkaTemplate.send(topic, key, payload)

        log.info(
            "Kafka message published. topic={}, key={}",
            topic,
            key
        )
    }
}