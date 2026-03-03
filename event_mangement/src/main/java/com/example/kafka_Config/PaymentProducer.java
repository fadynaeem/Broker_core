package com.example.kafka_Config;

import com.example.shared.event.PaymentEvent;
import com.example.shared.model.Channel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class PaymentProducer extends BaseProducer {
    private static final String TOPIC = Channel.PAYMENT.getTopicName();
    public PaymentProducer(KafkaTemplate<String, String> kafkaTemplate,
                           ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }
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
