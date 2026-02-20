package com.example.shared.event;
import java.io.Serializable;
import java.time.LocalDateTime;
public interface ProducerEvent extends Serializable {
    String getEventId();
    String getEventType();
    LocalDateTime getCreatedAt();
    String toJson() throws Exception;
}
