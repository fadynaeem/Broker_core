package com.example.mail.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
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

    @KafkaListener(topics = "${kafka.transaction-topic:transactions}", 
                   groupId = "${kafka.group-id:mail-observer-group}")
    public void handleTransactionEvent(String message) {
        try {
            logger.info("Received transaction event: {}", message);
            
            // Parse the message and extract transaction details
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> transactionData = 
                objectMapper.readValue(message, java.util.Map.class);
            
            String transactionId = (String) transactionData.get("id");
            if (transactionId == null) {
                transactionId = (String) transactionData.get("transactionId");
            }
            String status = (String) transactionData.get("status");
            String recipientEmail = (String) transactionData.get("userEmail");

            if (transactionId != null && status != null && recipientEmail != null) {
                mailNotificationService.sendTransactionAlert(recipientEmail, transactionId, status);
                logger.info("Mail notification sent for transaction: {}", transactionId);
            } else {
                logger.warn("Incomplete transaction data received: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing transaction event: {}", message, e);
        }
    }
}
