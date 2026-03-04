package com.example.shared.model;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DeliveryResult {
    private boolean success;
    private String messageId;
    private String errorMessage;
    private boolean transientError;
    private int httpStatusCode;
}
