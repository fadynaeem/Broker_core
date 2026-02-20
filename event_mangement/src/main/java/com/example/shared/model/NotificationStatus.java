package com.example.shared.model;

import lombok.Getter;
@Getter
public enum NotificationStatus {
    PENDING("pending"),
    SENT("sent"),
    DELIVERED("delivered"),
    FAILED("failed"),
    RETRY("retry");
    private final String value;
    NotificationStatus(String value) {
        this.value = value;
    }
}
