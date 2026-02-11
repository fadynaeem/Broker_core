package com.example.notification.model;

public enum Channel {
    PUSH("notifications-push"),
    EMAIL("notifications-email");
    private final String topicName;

    Channel(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getRetryTopicName() {
        return topicName + "-retry";
    }

    public String getDlqTopicName() {
        return topicName + "-dlq";
    }

    public String getDelayTopicName() {
        return topicName + "-delay";
    }
}
