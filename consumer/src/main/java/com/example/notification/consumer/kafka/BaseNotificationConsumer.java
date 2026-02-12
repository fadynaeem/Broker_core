package com.example.notification.consumer.kafka;

import com.example.notification.worker.processor.NotificationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;

/**
 * Base Kafka consumer for notification messages
 * Responsibility: Receive message from Kafka topic and delegate to worker
 */
@Slf4j
public abstract class BaseNotificationConsumer {

    @Autowired
    protected NotificationProcessor processor;

    protected abstract String getChannel();


    protected void handleKafkaMessage(String message, Acknowledgment acknowledgment) {
        try {
            log.debug("📨 Received message from Kafka topic for channel: {}", getChannel());
            
            var result = processor.processAndDeliver(
                message,
                getChannel()
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ Unexpected error handling Kafka message: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
