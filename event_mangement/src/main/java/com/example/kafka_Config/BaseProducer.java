package com.example.kafka_Config;

import com.example.shared.event.EventPublisher;
import com.example.shared.event.ProducerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * BaseProducer — shared publishing engine (ARCH TEAM).
 *
 * Implements {@link EventPublisher} with full send + callback logic.
 * All business producers extend this class — they MUST NOT replicate send logic.
 *
 * Injected from {@link KafkaProducerFactory}:
 *   - {@code KafkaTemplate<String, String>}
 *   - {@code ObjectMapper}
 *
 * Hierarchy position:
 *   KafkaBase
 *      └── KafkaProducerFactory ──(beans)──► BaseProducer  ← (you are here)
 *                                                  ▲
 *                                            PaymentProducer
 *                                            (Business layer)
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseProducer implements EventPublisher {

    protected final KafkaTemplate<String, String> kafkaTemplate;
    protected final ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // EventPublisher contract
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publish using the event's own ID as the partition key.
     */
    @Override
    public String publishEvent(ProducerEvent event, String topic) {
        return publishEventWithKey(event, topic, event.getEventId());
    }

    /**
     * Publish with an explicit partition key for controlled topic routing.
     */
    @Override
    public String publishEventWithKey(ProducerEvent event, String topic, String key) {
        String eventId = event.getEventId();
        try {
            String payload = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[{}] Published — topic={}, partition={}, offset={}, key={}",
                            producerName(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key);
                } else {
                    log.error("[{}] Publish failed — topic={}, key={}, eventId={}",
                            producerName(), topic, key, eventId, ex);
                }
            });

            return eventId;

        } catch (Exception e) {
            log.error("[{}] Serialisation error — eventId={}", producerName(), eventId, e);
            throw new RuntimeException("Failed to publish event: " + eventId, e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subclass hook
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a human-readable producer name used in log messages.
     * Override in each business producer for clear log attribution.
     */
    protected String producerName() {
        return getClass().getSimpleName();
    }
}
