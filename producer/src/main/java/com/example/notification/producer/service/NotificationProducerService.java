package com.example.notification.producer.service;

import com.example.notification.shared.model.Channel;
import com.example.notification.shared.model.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${kafka.email.topic:notifications-email}")
    private String emailTopic;
    public String sendSimpleEmail(String recipient, String subject, String message) {
        Map<String, String> params = new HashMap<>();
        params.put("message", message);
        return sendNotification(Channel.EMAIL, "default", recipient, subject, "simple-email", params);
    }

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
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(emailTopic, dedupeId, messageJson);
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
}
