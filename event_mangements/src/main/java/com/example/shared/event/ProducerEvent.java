package com.example.shared.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Generic producer event interface for creating events across any producer
 */
public interface ProducerEvent extends Serializable {
    
    String getEventId();
    
    String getEventType();
    
    LocalDateTime getCreatedAt();
    
    String toJson() throws Exception;
}
