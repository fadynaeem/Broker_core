package com.example.notification.payment.service;

import com.example.notification.payment.model.Payment;
import com.example.notification.payment.model.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PaymentTransactionService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.payment.topic:payment-events}")
    private String paymentTopic;

    public String publishPaymentEvent(
            String userId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description
    ) {
        return publishPayment(userId, amount, currency, paymentMethod, PaymentStatus.PENDING, description, null);
    }

    public String publishPayment(
            String userId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            PaymentStatus status,
            String description,
            String failureReason
    ) {
        try {
            String transactionId = UUID.randomUUID().toString();
            
            Payment payment = Payment.builder()
                    .transactionId(transactionId)
                    .userId(userId)
                    .amount(amount)
                    .currency(currency)
                    .paymentMethod(paymentMethod)
                    .status(status)
                    .description(description)
                    .failureReason(failureReason)
                    .createdAt(LocalDateTime.now())
                    .build();

            String paymentJson = objectMapper.writeValueAsString(payment);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    paymentTopic, 
                    transactionId, 
                    paymentJson
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Payment event published successfully to topic: {}, partition: {}, offset: {}, transactionId: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            transactionId);
                } else {
                    log.error("Failed to publish payment event to Kafka", ex);
                }
            });

            return transactionId;
        } catch (Exception e) {
            log.error("Error publishing payment event to Kafka", e);
            throw new RuntimeException("Failed to publish payment event", e);
        }
    }
}
