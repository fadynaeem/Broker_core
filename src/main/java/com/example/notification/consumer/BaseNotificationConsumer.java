package com.example.notification.consumer;

import com.example.notification.adapter.DeliveryAdapter;
import com.example.notification.model.Channel;
import com.example.notification.model.DeliveryResult;
import com.example.notification.model.NotificationMessage;
import com.example.notification.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;

/**
 * Simple Kafka consumer - Perfect for beginners!
 * Flow: Receive message → Parse → Render → Send
 */
@Slf4j
public abstract class BaseNotificationConsumer {

    @Autowired
    protected TemplateService templateService;

    @Autowired
    protected ObjectMapper objectMapper;

    protected abstract Channel getChannel();
    protected abstract DeliveryAdapter getDeliveryAdapter();

    /**
     * Main method to process Kafka messages - Simple and easy to understand
     */
    protected void processMessage(String message, Acknowledgment acknowledgment) {
        try {
            // Step 1: Parse JSON message
            NotificationMessage notification = objectMapper.readValue(message, NotificationMessage.class);
            log.info("📥 Received {} notification for: {}", getChannel(), notification.getRecipient());
            
            // Step 2: Get message content
            String content = templateService.render(
                notification.getTemplateId(), 
                notification.getTemplateParams()
            );
            
            // Step 3: Send via adapter (email, SMS, etc.)
            DeliveryResult result = getDeliveryAdapter().deliver(notification, content);
            
            // Step 4: Log result
            if (result.isSuccess()) {
                log.info("✅ Sent successfully!");
            } else {
                log.error("❌ Failed: {}", result.getErrorMessage());
            }
            
            // Step 5: Acknowledge Kafka message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
