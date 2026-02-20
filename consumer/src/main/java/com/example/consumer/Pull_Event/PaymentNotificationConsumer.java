package com.example.consumer.Pull_Event;

import com.example.shared.model.Channel;
import com.example.worker.processor.ProcessingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class PaymentNotificationConsumer extends BaseNotificationConsumer {
    private final PaymentConfirmationPublisher confirmationPublisher;
    public PaymentNotificationConsumer(PaymentConfirmationPublisher confirmationPublisher) {
        this.confirmationPublisher = confirmationPublisher;
    }
    @Override
    protected String getChannel() {
        return Channel.PAYMENT.name();
    }
    @KafkaListener(
            topics = "${kafka.payment.topic}",
            groupId = "payment-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFromPaymentTopic(String message, Acknowledgment acknowledgment) {
        log.debug("Message arrived on notifications-payment topic");
        ProcessingResult result = processor.processAndDeliver(message, getChannel());
        if (result.isSuccess()) {
            confirmationPublisher.publishCompleted(result.getPayment());
            acknowledgment.acknowledge();
            return;
        }
        log.warn("Payment processing failed. Message will be retried.");
    }
    @KafkaListener(
            topics = "${kafka.payment.retry.topic}",
            groupId = "payment-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFromRetryTopic(String message, Acknowledgment acknowledgment) {
        log.debug("Message arrived on notifications-payment-retry topic");
        ProcessingResult result = processor.processAndDeliver(message, getChannel());
        if (result.isSuccess()) {
            confirmationPublisher.publishCompleted(result.getPayment());
            acknowledgment.acknowledge();
            return;
        }
        log.warn("Payment processing failed on retry. Message will be retried again.");
    }
}
