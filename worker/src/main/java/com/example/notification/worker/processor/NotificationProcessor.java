package com.example.notification.worker.processor;

import com.example.notification.shared.model.DeliveryResult;
import com.example.notification.shared.model.NotificationMessage;
import com.example.notification.worker.adapter.DeliveryAdapter;
import com.example.notification.worker.adapter.SendGridEmailAdapter;
import com.example.notification.worker.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Main notification processing service
 * Handles: Parse → Render → Deliver workflow
 */
@Slf4j
@Service
public class NotificationProcessor {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SendGridEmailAdapter emailAdapter;

    @Autowired
    private ObjectMapper objectMapper;

    public ProcessingResult processAndDeliver(String message, String channel) {
        try {
            log.info("⚙️  Processing {} notification from Kafka", channel);
            
            NotificationMessage notification = objectMapper.readValue(message, NotificationMessage.class);
            log.info("📥 Received notification for: {}", notification.getRecipient());
            
            String content = templateService.render(
                notification.getTemplateId(), 
                notification.getTemplateParams()
            );
            log.debug("📝 Template rendered for templateId: {}", notification.getTemplateId());
            
            DeliveryResult result = deliverByChannel(notification, content, channel);
            
            if (result.isSuccess()) {
                log.info("✅ Notification delivered successfully! MessageId: {}", result.getMessageId());
                return ProcessingResult.success(notification, result);
            } else {
                log.error("❌ Failed to deliver notification: {}", result.getErrorMessage());
                return ProcessingResult.failure(notification, result);
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing notification: {}", e.getMessage(), e);
            return ProcessingResult.error(message, e);
        }
    }

    private DeliveryResult deliverByChannel(NotificationMessage notification, String content, String channel) {
        return switch (channel.toUpperCase()) {
            case "EMAIL" -> emailAdapter.deliver(notification, content);
            default -> {
                log.warn("Unknown channel: {}", channel);
                yield DeliveryResult.builder()
                        .success(false)
                        .errorMessage("Unknown channel: " + channel)
                        .build();
            }
        };
    }
}
