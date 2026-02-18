package com.example.consumer.kafka;

import com.example.shared.event.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConfirmationPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.payment.confirm.topic:payment-confirmations}")
    private String confirmationTopic;

    public void publishCompleted(PaymentEvent paymentEvent) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("transactionId", paymentEvent.getTransactionId());
            payload.put("status", "COMPLETED");
            payload.put("userEmail", paymentEvent.getUserEmail());
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(confirmationTopic, paymentEvent.getTransactionId(), json);
            log.info("Published payment confirmation for transactionId: {}", paymentEvent.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish payment confirmation", e);
            throw new RuntimeException("Failed to publish payment confirmation", e);
        }
    }
}
