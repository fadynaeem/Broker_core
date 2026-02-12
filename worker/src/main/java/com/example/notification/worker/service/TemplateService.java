package com.example.notification.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    public String render(String templateId, Map<String, String> params) {
        if (templateId == null) {
            return "No template specified";
        }

        return switch (templateId) {
            case "simple-email" -> renderSimpleEmail(params);
            case "notification-generic" -> renderGenericNotification(params);
            default -> renderDefault(templateId, params);
        };
    }

    private String renderSimpleEmail(Map<String, String> params) {
        String message = params != null ? params.getOrDefault("message", "") : "";
        return String.format(
                "<html><body><p>%s</p></body></html>",
                message
        );
    }

    private String renderGenericNotification(Map<String, String> params) {
        String message = params != null ? params.getOrDefault("message", "Notification") : "Notification";
        return String.format(
                "<html><body><h2>Notification</h2><p>%s</p></body></html>",
                message
        );
    }

    private String renderDefault(String templateId, Map<String, String> params) {
        log.warn("Template not found: {}, using default rendering", templateId);
        String content = params != null && !params.isEmpty() 
                ? params.values().stream().findFirst().orElse("No content")
                : "No content";
        return String.format(
                "<html><body><p>%s</p></body></html>",
                content
        );
    }
}
