package com.example.shared.model;
import lombok.Getter;
@Getter
public enum Channel {
    PAYMENT("payment-events"),
    EMAIL("email-events");
    private final String topicName;
    Channel(String topicName) {
        this.topicName = topicName;
    }
}
