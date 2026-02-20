package com.example.shared.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Override
    public String publishEvent(ProducerEvent event, String topic) {
        return publishEventWithKey(event, topic, event.getEventId());
    }
    @Override
    public String publishEventWithKey(ProducerEvent event, String topic, String key) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String eventId = event.getEventId();

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    topic,
                    key,
                    eventJson
            );
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published successfully to topic: {}, partition: {}, offset: {}, eventId: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            eventId);
                } else {
                    log.error("Failed to publish event to Kafka", ex);
                }
            });
            return eventId;
        } catch (Exception e) {
            log.error("Error publishing event to Kafka", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
