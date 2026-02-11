package com.example.notification.consumer;

import com.example.notification.model.Channel;
import com.example.notification.model.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class MessageParser {
    private final ObjectMapper objectMapper;
    public MessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    /**
     * Parse JSON message to NotificationMessage object
     * @param message JSON string message
     * @param channel notification channel for logging
     * @return parsed NotificationMessage
     * @throws Exception if parsing fails
     */
    public NotificationMessage parse(String message, Channel channel) throws Exception {
        NotificationMessage notification = objectMapper.readValue(message, NotificationMessage.class);
        log.info(" Parsed {} notification for recipient: {}, dedupeId: {}", 
                channel, notification.getRecipient(), notification.getDedupeId());
        return notification;
    }
}
