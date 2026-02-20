package com.example.payment.service;

import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.example.shared.event.EventPublisher;
import com.example.shared.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
@Slf4j
@Service
public class PaymentTransactionService {
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private PaymentRepository paymentRepository;
    @Value("${kafka.payment.topic:payment-events}")
    private String paymentTopic;
    public String publishPaymentEvent(PaymentRequestDTO request) {
        String transactionId = UUID.randomUUID().toString();
        String description = request.getDescription() != null ? request.getDescription() : "Payment transaction";
        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
        PaymentEvent event = PaymentEvent.builder()
                .transactionId(transactionId)
                .userEmail(request.getUserId())
                .amount(request.getAmount().toPlainString())
                .currency(request.getCurrency())
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        return eventPublisher.publishEvent(event, paymentTopic);
    }
}
