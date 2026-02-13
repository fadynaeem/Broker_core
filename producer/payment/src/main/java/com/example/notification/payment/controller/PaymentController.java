package com.example.notification.payment.controller;

import com.example.notification.payment.service.PaymentTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processPayment(
            @RequestParam String userId,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "CREDIT_CARD") String paymentMethod,
            @RequestParam(required = false) String description
    ) {
        log.info("Processing payment for user: {}, amount: {} {}", userId, amount, currency);
        
        String transactionId = paymentTransactionService.publishPaymentEvent(
                userId,
                amount,
                currency,
                paymentMethod,
                description != null ? description : "Payment transaction"
        );

        Map<String, String> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("status", "queued");
        response.put("userId", userId);
        response.put("amount", amount.toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-transaction-service");
        return ResponseEntity.ok(response);
    }
}
