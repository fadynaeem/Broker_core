package com.example.notification.shared.model;

import lombok.Getter;

@Getter
public enum Channel {
    EMAIL("notifications-email"),
    SMS("notifications-sms"),
    PUSH("notifications-push");

    private final String topicName;

    Channel(String topicName) {
        this.topicName = topicName;
    }
}
