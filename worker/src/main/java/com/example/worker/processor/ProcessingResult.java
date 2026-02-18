package com.example.worker.processor;

import com.example.shared.model.DeliveryResult;
import com.example.shared.event.PaymentEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
@AllArgsConstructor
public class ProcessingResult {
    private boolean success;
    private PaymentEvent payment;
    private DeliveryResult deliveryResult;
    private String errorMessage;
    private Exception exception;
    public static ProcessingResult success(PaymentEvent payment, DeliveryResult deliveryResult) {
        return ProcessingResult.builder()
                .success(true)
                .payment(payment)
                .deliveryResult(deliveryResult)
                .build();
    }
    public static ProcessingResult failure(PaymentEvent payment, DeliveryResult deliveryResult) {
        return ProcessingResult.builder()
                .success(false)
                .payment(payment)
                .deliveryResult(deliveryResult)
                .errorMessage(deliveryResult.getErrorMessage())
                .build();
    }
    public static ProcessingResult error(String message, Exception e) {
        return ProcessingResult.builder()
                .success(false)
                .errorMessage("Failed to parse message: " + e.getMessage())
                .exception(e)
                .build();
    }
}
