package com.example.notification.adapter;

import com.example.notification.model.DeliveryResult;
import com.example.notification.model.NotificationMessage;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendGridEmailAdapter implements DeliveryAdapter {

    @Value("${sendgrid.api-key:MOCK}")
    private String apiKey;

    @Value("${sendgrid.from-email:notifications@example.com}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Notification System}")
    private String fromName;

    @Value("${sendgrid.mock-mode:true}")
    private boolean mockMode;

    @Override
    public DeliveryResult deliver(NotificationMessage message, String renderedContent) {
        try {
            if (mockMode || "MOCK".equals(apiKey)) {
                log.info("📧 EMAIL NOTIFICATION (MOCK MODE)");
                log.info("To: {}", message.getRecipient());
                log.info("Subject: {}", message.getSubject());
                log.info("Content: {}", renderedContent);
                log.info("Tenant: {}", message.getTenantId());
                
                return DeliveryResult.builder()
                        .success(true)
                        .messageId("MOCK-EMAIL-" + System.currentTimeMillis())
                        .build();
            }

            // Real SendGrid integration
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(message.getRecipient());
            String subject = message.getSubject() != null ? message.getSubject() : "Notification";
            Content content = new Content("text/html", renderedContent);
            
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully via SendGrid. Status: {}", response.getStatusCode());
                return DeliveryResult.builder()
                        .success(true)
                        .messageId(response.getHeaders().getOrDefault("X-Message-Id", "unknown"))
                        .httpStatusCode(response.getStatusCode())
                        .build();
            } else {
                log.error("SendGrid returned error. Status: {}, Body: {}", 
                         response.getStatusCode(), response.getBody());
                return DeliveryResult.builder()
                        .success(false)
                        .errorMessage(response.getBody())
                        .transientError(response.getStatusCode() >= 500)
                        .httpStatusCode(response.getStatusCode())
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to send email via SendGrid", e);
            return DeliveryResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .transientError(isTransientError(e))
                    .build();
        }
    }

    @Override
    public boolean supportsBatching() {
        return true;
    }

    private boolean isTransientError(Exception e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("timeout") ||
                message.contains("connection") ||
                message.contains("unavailable")
        );
    }
}
