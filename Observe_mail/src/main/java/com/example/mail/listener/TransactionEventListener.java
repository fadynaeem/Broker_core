package com.example.mail.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.mail.service.MailNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class TransactionEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);
    private final MailNotificationService mailNotificationService;
    private final ObjectMapper objectMapper;
    public TransactionEventListener(MailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
        this.objectMapper = new ObjectMapper();
    }
    @KafkaListener(
            topics   = "${kafka.payment.confirm.topic:payment-confirmations}",
            groupId  = "${kafka.group-id:notification-observer-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentConfirmation(@Payload String message, Acknowledgment acknowledgment) {
        try {
            logger.info("Received payment confirmation from notify queue: {}", message);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data =objectMapper.readValue(message, java.util.Map.class);
            String transactionId = (String) data.get("transactionId");
            String status = (String) data.get("status");
            String recipientEmail = (String) data.get("userEmail");
            if (transactionId != null && status != null && recipientEmail != null) {
                mailNotificationService.sendTransactionAlert(recipientEmail, transactionId, status);
                logger.info("Mail notification sent for transaction: {}", transactionId);
                acknowledgment.acknowledge();
            } else {
                logger.warn("Incomplete payment confirmation data – skipping: {}", message);
                acknowledgment.acknowledge(); 
            }
        } catch (Exception e) {
            logger.error("Error processing payment confirmation event: {}", message, e);
        }
    }
}

