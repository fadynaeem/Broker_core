package com.example.notification.worker.adapter;

import com.example.notification.shared.model.DeliveryResult;
import com.example.notification.shared.model.NotificationMessage;

public interface DeliveryAdapter {
    DeliveryResult deliver(NotificationMessage message, String renderedContent);
    boolean supportsBatching();
}
