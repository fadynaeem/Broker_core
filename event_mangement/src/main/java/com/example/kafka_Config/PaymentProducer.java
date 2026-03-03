package com.example.kafka_Config;

import com.example.shared.event.PaymentEvent;
import com.example.shared.model.Channel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * PaymentProducer — Business layer producer (NewProducer).
 *
 * Extends {@link BaseProducer} to gain all send + retry logic for free.
 * Owns everything specific to the PAYMENT domain:
 *  - Which topic to target ({@link Channel#PAYMENT})
 *  - Domain-level convenience methods (publishPayment, publishPaymentWithKey)
 *
 * Teams adding new event types should follow this pattern:
 *   1. Add a new Channel enum entry → TopicManager auto-creates the topic.
 *   2. Create a new *Producer extends BaseProducer for the domain.
 *
 * Hierarchy position:
 *   KafkaBase
 *      └── KafkaProducerFactory ──(beans)──► BaseProducer
 *                                                  ▲
 *                                           PaymentProducer  ← (you are here)
 */
@Slf4j
@Component
public class PaymentProducer extends BaseProducer {

    private static final String TOPIC = Channel.PAYMENT.getTopicName();

    public PaymentProducer(KafkaTemplate<String, String> kafkaTemplate,
                           ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain-level API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publish a payment event using the transaction ID as the partition key.
     * Guarantees that all events for the same transaction land on the same partition.
     *
     * @param event the payment event to publish
     * @return the event ID (transactionId)
     */
    public String publishPayment(PaymentEvent event) {
        log.debug("[PaymentProducer] Sending payment event — transactionId={}", event.getEventId());
        return publishEvent(event, TOPIC);
    }

    /**
     * Publish a payment event with an explicit partition key.
     * Use when you need to group by a different attribute (e.g. tenantId).
     *
     * @param event the payment event to publish
     * @param key   explicit partition key
     * @return the event ID (transactionId)
     */
    public String publishPaymentWithKey(PaymentEvent event, String key) {
        log.debug("[PaymentProducer] Sending payment event — transactionId={}, key={}", event.getEventId(), key);
        return publishEventWithKey(event, TOPIC, key);
    }

    @Override
    protected String producerName() {
        return "PaymentProducer";
    }
}
