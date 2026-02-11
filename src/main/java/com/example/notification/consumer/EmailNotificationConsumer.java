package com.example.notification.consumer;

import com.example.notification.adapter.DeliveryAdapter;
import com.example.notification.adapter.SendGridEmailAdapter;
import com.example.notification.model.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationConsumer extends BaseNotificationConsumer {

    @Autowired
    private SendGridEmailAdapter emailAdapter;

    @Override
    protected Channel getChannel() {
        return Channel.EMAIL;
    }

    @Override
    protected DeliveryAdapter getDeliveryAdapter() {
        return emailAdapter;
    }

    @KafkaListener(
            topics = "notifications-email",
            groupId = "email-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message, Acknowledgment acknowledgment) {
        log.debug("Received email notification message");
        processMessage(message, acknowledgment);
    }

    @KafkaListener(
            topics = "notifications-email-retry",
            groupId = "email-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRetry(String message, Acknowledgment acknowledgment) {
        log.debug("Received email retry message");
        processMessage(message, acknowledgment);
    }
}
