package com.example.notification.controller;

import com.example.notification.model.Channel;
import com.example.notification.service.NotificationProducerService;
import com.example.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationProducerService producerService;

    /**
     * Send email notification (simple beginner-friendly endpoint)
     */
    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(
            @RequestParam String recipient,
            @RequestParam String subject,
            @RequestParam String message) {
        
        log.info("📧 Sending email to: {}", recipient);

        // Send to Kafka
        String messageId = producerService.sendSimpleEmail(recipient, subject, message);

        Map<String, String> response = new HashMap<>();
        response.put("status", "queued");
        response.put("messageId", messageId);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "notification-system");
        return ResponseEntity.ok(response);
    }
}
