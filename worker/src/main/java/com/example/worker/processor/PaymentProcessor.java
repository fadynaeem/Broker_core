package com.example.worker.processor;

import com.example.shared.model.DeliveryResult;
import com.example.shared.event.PaymentEvent;
import com.example.worker.adapter.StripePaymentAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class PaymentProcessor {
    @Autowired
    private StripePaymentAdapter paymentAdapter;
    @Autowired
    private ObjectMapper objectMapper;
    public ProcessingResult processAndDeliver(String message, String channel) {
        try {
            log.info("Processing {} payment from Kafka", channel);
            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            log.info("Received payment request for: {}", paymentEvent.getUserEmail());
            DeliveryResult result = paymentAdapter.deliver(paymentEvent);
            if (result.isSuccess()) {
                log.info("Payment processed successfully! MessageId: {}", result.getMessageId());
                return ProcessingResult.success(paymentEvent, result);
            } else {
                log.error("Failed to process payment: {}", result.getErrorMessage());
                return ProcessingResult.failure(paymentEvent, result);
            }
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return ProcessingResult.error(message, e);
        }
    }
}
