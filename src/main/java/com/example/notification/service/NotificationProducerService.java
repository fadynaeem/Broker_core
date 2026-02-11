package com.example.notification.service;

import com.example.notification.model.Channel;
import com.example.notification.model.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Simple method to send email via Kafka - Beginner friendly!
     */
    public String sendSimpleEmail(String recipient, String subject, String message) {
        try {
            // 1. Generate unique ID
            String messageId = UUID.randomUUID().toString();

            // 2. Prepare message data
            Map<String, String> params = new HashMap<>();
            params.put("message", message);

            // 3. Build notification object
            NotificationMessage notification = NotificationMessage.builder()
                    .dedupeId(messageId)
                    .channel(Channel.EMAIL)
                    .tenantId("default")
                    .recipient(recipient)
                    .subject(subject)
                    .templateId("simple-email")
                    .templateParams(params)
                    .createdAt(LocalDateTime.now())
                    .build();

            // 4. Convert to JSON
            String jsonMessage = objectMapper.writeValueAsString(notification);

            // 5. Send to Kafka
            String topic = Channel.EMAIL.getTopicName();
            log.info("📤 Sending to Kafka topic: {}", topic);

            kafkaTemplate.send(topic, messageId, jsonMessage);
            log.info("✅ Message queued!");

            return messageId;

        } catch (Exception e) {
            log.error("❌ Send failed: {}", e.getMessage());
            throw new RuntimeException("Failed to send", e);
        }
    }

    // ========== Advanced Methods (can be removed for simpler version) ==========

    /**
     * Send a notification message to Kafka
     * @param channel notification channel (PUSH, EMAIL, SMS)
     * @param tenantId tenant identifier
     * @param recipient recipient address (email, phone, device token)
     * @param subject notification subject (optional for SMS)
     * @param templateId template identifier
     * @param templateParams template parameters
     * @return deduplication ID
     */
    public String sendNotification(
            Channel channel,
            String tenantId,
            String recipient,
            String subject,
            String templateId,
            Map<String, String> templateParams
    ) {
        return sendNotification(channel, tenantId, recipient, subject, templateId, templateParams, null, null);
    }

    /**
     * Send a notification message to Kafka with scheduling and metadata
     * @param channel notification channel
     * @param tenantId tenant identifier
     * @param recipient recipient address
     * @param subject notification subject
     * @param templateId template identifier
     * @param templateParams template parameters
     * @param sendAt scheduled delivery time (null for immediate)
     * @param metadata additional metadata
     * @return deduplication ID
     */
    public String sendNotification(
            Channel channel,
            String tenantId,
            String recipient,
            String subject,
            String templateId,
            Map<String, String> templateParams,
            LocalDateTime sendAt,
            Map<String, String> metadata
    ) {
        try {
            String dedupeId = UUID.randomUUID().toString();

            NotificationMessage message = NotificationMessage.builder()
                    .dedupeId(dedupeId)
                    .channel(channel)
                    .tenantId(tenantId)
                    .recipient(recipient)
                    .subject(subject)
                    .templateId(templateId)
                    .templateParams(templateParams)
                    .sendAt(sendAt)
                    .metadata(metadata)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            String messageJson = objectMapper.writeValueAsString(message);
            String topic = channel.getTopicName();

            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(topic, dedupeId, messageJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent successfully to topic: {}, partition: {}, offset: {}, dedupeId: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            dedupeId);
                } else {
                    log.error("Failed to send message to Kafka", ex);
                }
            });

            return dedupeId;

        } catch (Exception e) {
            log.error("Error sending notification to Kafka", e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    /**
     * Quick send method for simple notifications
     * @param channel notification channel
     * @param recipient recipient address
     * @param message simple message content
     * @return deduplication ID
     */
    public String sendSimpleNotification(Channel channel, String recipient, String message) {
        Map<String, String> params = Map.of("message", message);
        return sendNotification(
                channel,
                "default-tenant",
                recipient,
                "Notification",
                "notification-generic",
                params
        );
    }
}
