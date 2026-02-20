package com.example.consumer.Pull_Event;

import com.example.shared.model.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class EmailNotificationConsumer extends BaseNotificationConsumer {
    @Override
    protected String getChannel() {
        return Channel.EMAIL.name();
    }
    @KafkaListener(
            topics = {"${kafka.email.topic}", "${kafka.email.retry.topic}"},
            groupId = "email-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFromEmailTopics(String message, Acknowledgment acknowledgment) {
        log.debug("Message arrived on email topic");
        handleKafkaMessage(message, acknowledgment);
    }
}
