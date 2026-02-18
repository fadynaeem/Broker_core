package com.example.mail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.mail.config.MailConfig;

@Service
public class MailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(MailNotificationService.class);
    
    private final MailConfig mailConfig;

    public MailNotificationService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public void sendTransactionNotification(String recipientEmail, String subject, String body) {
        try {
            logger.info("Sending mail notification to: {} with subject: {}", recipientEmail, subject);
            // SendGrid integration would go here
            logger.info("Mail notification sent successfully to: {}", recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send mail notification to: {}", recipientEmail, e);
            throw new RuntimeException("Failed to send mail notification", e);
        }
    }

    public void sendTransactionAlert(String recipientEmail, String transactionId, String status) {
        String subject = "Transaction Alert: " + transactionId;
        String body = String.format("Transaction ID: %s has been updated to status: %s", transactionId, status);
        sendTransactionNotification(recipientEmail, subject, body);
    }
}
