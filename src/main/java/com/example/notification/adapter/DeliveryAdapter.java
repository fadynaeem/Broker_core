package com.example.notification.adapter;

import com.example.notification.model.DeliveryResult;
import com.example.notification.model.NotificationMessage;

/**
 * Interface for delivery adapters
 */
public interface DeliveryAdapter {
    
    /**
     * Deliver a notification message
     * @param message the notification message
     * @param renderedContent the rendered content after template processing
     * @return delivery result
     */
    DeliveryResult deliver(NotificationMessage message, String renderedContent);
    
    /**
     * Check if this adapter supports batching
     * @return true if batching is supported
     */
    default boolean supportsBatching() {
        return false;
    }
}
