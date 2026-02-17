package com.example.notification.payment.listener;

import com.example.notification.payment.model.Payment;
import com.example.notification.payment.model.PaymentStatus;
import com.example.notification.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class PaymentConfirmationListener {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    public PaymentConfirmationListener(PaymentRepository paymentRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
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
            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Payment confirmed for transactionId: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to process confirmation message: {}", message, e);
        }
    }
}
