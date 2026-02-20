package com.example.consumer.Pull_Event;

import com.example.worker.processor.PaymentProcessor;
import com.example.worker.processor.ProcessingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
@Slf4j
public abstract class BaseNotificationConsumer {
    @Autowired
    protected PaymentProcessor processor;
    protected abstract String getChannel();
    protected void handleKafkaMessage(String message, Acknowledgment acknowledgment) {
        try {
            log.debug("Received message from Kafka topic for channel: {}", getChannel());
            ProcessingResult result = processor.processAndDeliver(message, getChannel());
            if (result.isSuccess()) {
                acknowledgment.acknowledge();
                return;
            }
            log.warn("Processing failed for channel: {}. Message will be retried.", getChannel());
        } catch (Exception e) {
            log.error("Unexpected error handling Kafka message: {}", e.getMessage(), e);
        }
    }
}
