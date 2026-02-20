package com.example.notification.producer.controller;

import com.example.notification.shared.model.Channel;
import com.example.notification.producer.service.NotificationProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/addnotification")
public class NotificationController {
    @Autowired
    private NotificationProducerService producerService;
    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(
            @RequestParam String recipient,
            @RequestParam String subject,
            @RequestParam String message) {
        log.info("Sending email to: {}", recipient);
        String messageId = producerService.sendSimpleEmail(recipient, subject, message);
        Map<String, String> response = new HashMap<>();
        response.put("status", "queued");
        response.put("messageId", messageId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/ok")
    public ResponseEntity<Map<String, String>> ok() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "notification-producer");
        return ResponseEntity.ok(response);
    }
}
