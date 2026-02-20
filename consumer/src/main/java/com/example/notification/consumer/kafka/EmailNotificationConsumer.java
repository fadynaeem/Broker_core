package com.example.notification.consumer.kafka;

import com.example.notification.shared.model.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Email Notification Kafka Consumer
 * Listens to email notification topics and delegates processing to worker service
 */
@Slf4j
@Component
public class EmailNotificationConsumer extends BaseNotificationConsumer {

    @Override
    protected String getChannel() {
        return Channel.EMAIL.name();
    }

    @KafkaListener(
            topics = "notifications-email",
            groupId = "email-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFromEmailTopic(String message, Acknowledgment acknowledgment) {
        log.debug("Message arrived on notifications-email topic");
        handleKafkaMessage(message, acknowledgment);
    }

    @KafkaListener(
            topics = "notifications-email-retry",
            groupId = "email-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFromRetryTopic(String message, Acknowledgment acknowledgment) {
        log.debug("Message arrived on notifications-email-retry topic");
        handleKafkaMessage(message, acknowledgment);
    }
}
