package com.example.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResult {
    private boolean success;
    private String messageId;
    private String errorMessage;
    private boolean transientError;
    private int httpStatusCode;
}
