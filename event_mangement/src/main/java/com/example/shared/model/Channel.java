package com.example.shared.model;
import lombok.Getter;
@Getter
public enum Channel {
    EMAIL("notifications-email"),
    PAYMENT("notifications-payment");
    private final String topicName;
    Channel(String topicName) {
        this.topicName = topicName;
    }
}
