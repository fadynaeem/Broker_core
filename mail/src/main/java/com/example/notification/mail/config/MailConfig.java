package com.example.notification.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Value("${sendgrid.api-key:}")
    private String sendgridApiKey;

    @Value("${mail.from-email:notifications@example.com}")
    private String fromEmail;

    @Value("${mail.from-name:Notification System}")
    private String fromName;

    public String getSendgridApiKey() {
        return sendgridApiKey;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getFromName() {
        return fromName;
    }
}
