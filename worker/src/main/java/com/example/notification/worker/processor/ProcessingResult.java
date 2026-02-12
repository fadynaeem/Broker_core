package com.example.notification.worker.processor;

import com.example.notification.shared.model.DeliveryResult;
import com.example.notification.shared.model.NotificationMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Result of processing and delivering a notification
 */
@Data
@Builder
@AllArgsConstructor
public class ProcessingResult {
    private boolean success;
    private NotificationMessage notification;
    private DeliveryResult deliveryResult;
    private String errorMessage;
    private Exception exception;

    public static ProcessingResult success(NotificationMessage notification, DeliveryResult deliveryResult) {
        return ProcessingResult.builder()
                .success(true)
                .notification(notification)
                .deliveryResult(deliveryResult)
                .build();
    }

    public static ProcessingResult failure(NotificationMessage notification, DeliveryResult deliveryResult) {
        return ProcessingResult.builder()
                .success(false)
                .notification(notification)
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
