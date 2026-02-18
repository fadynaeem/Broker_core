package com.example.payment.controller;

import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private PaymentTransactionService paymentTransactionService;
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        log.info("Processing payment for user: {}, amount: {} {}", request.getUserId(), request.getAmount(), request.getCurrency());
        String transactionId = paymentTransactionService.publishPaymentEvent(request);
        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .transactionId(transactionId)
                .status("queued")
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .build();
        return ResponseEntity.ok(response);
    }
}
