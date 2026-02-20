package com.example.payment.listener;

import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Component
public class PaymentConfirmationListener {
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.mail.topic:maill}")
    private String mailTopic;

    public PaymentConfirmationListener(PaymentRepository paymentRepository, ObjectMapper objectMapper,
                                       KafkaTemplate<String, String> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    @KafkaListener(topics = "${kafka.payment.confirm.topic:payment-confirmations}",
            groupId = "${kafka.payment.confirm.group:payment-confirmations-group}")
    public void handleConfirmation(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);

            String transactionId = (String) payload.get("transactionId");
            if (transactionId == null) {
                transactionId = (String) payload.get("id");
            }

            if (transactionId == null || transactionId.isBlank()) {
                log.warn("Confirmation message missing transactionId: {}", message);
                return;
            }

            Optional<Payment> paymentOpt = paymentRepository.findById(transactionId);
            if (paymentOpt.isEmpty()) {
                log.warn("No payment found for transactionId: {}", transactionId);
                return;
            }

            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            publishMailEvent(payment);
            log.info("Payment completed for transactionId: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to process confirmation message: {}", message, e);
        }
    }
    private void publishMailEvent(Payment payment) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", payment.getTransactionId());
        payload.put("status", payment.getStatus().name());
        payload.put("userEmail", payment.getUserId());
        String json = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send(mailTopic, payment.getTransactionId(), json);
    }
}
