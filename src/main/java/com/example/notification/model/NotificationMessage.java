package com.example.notification.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    @NotBlank(message = "Dedupe ID is required")
    private String dedupeId;
    @NotNull(message = "Channel is required")
    private Channel channel;
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    @NotBlank(message = "Recipient is required")
    private String recipient;
    private String subject;
    @NotBlank(message = "Template ID is required")
    private String templateId;
    private Map<String, String> templateParams;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sendAt;
    private Integer priority;
    private Map<String, String> metadata;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private Integer retryCount;
    private String lastError;
}
