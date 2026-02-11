package com.example.notification.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");
    private final Map<String, String> templates = new HashMap<>();

    public TemplateService() {
        templates.put("welcome-email", "Hello {{name}}, Welcome to {{appName}}!");
        templates.put("otp-sms", "Your OTP is {{otp}}. Valid for {{validity}} minutes.");
        templates.put("order-push", "Order {{orderId}} has been {{status}}. Track it here: {{trackingUrl}}");
        templates.put("reset-password", "Hi {{name}}, Click here to reset your password: {{resetUrl}}");
        templates.put("notification-generic", "{{message}}");
    }

    /**
     * Simple renderer - Just returns the message (Perfect for beginners!)
     * Advanced: You can add template logic later
     */
    public String render(String templateId, Map<String, String> params) {
        // For beginners: just return the message directly
        if (params != null && params.containsKey("message")) {
            return params.get("message");
        }
        
        // Fallback to template
        String template = templates.get(templateId);
        if (template != null) {
            return template;
        }
        
        return "Notification";
    }
    
    // ========== Advanced Template Logic (for scaling later) ==========
    
    /**
     * Advanced: Render with placeholders like {{name}}
     */
    public String renderAdvanced(String templateId, Map<String, String> params) {
        String template = templates.get(templateId);
        if (template == null) {
            return params.getOrDefault("message", "Notification");
        }
        if (params == null || params.isEmpty()) {
            return template;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String replacement = params.getOrDefault(key, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Register a new template
     * @param templateId template identifier
     * @param template template content with {{placeholder}} syntax
     */
    public void registerTemplate(String templateId, String template) {
        templates.put(templateId, template);
    }

    /**
     * Check if a template exists
     * @param templateId template identifier
     * @return true if template exists
     */
    public boolean hasTemplate(String templateId) {
        return templates.containsKey(templateId);
    }
}
