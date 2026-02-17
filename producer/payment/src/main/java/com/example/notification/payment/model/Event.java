package com.example.notification.payment.model;

import com.example.notification.shared.event.ProducerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event implements ProducerEvent {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    @NotBlank(message = "User ID is required")
    private String userId;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    @NotBlank(message = "Currency is required")
    private String currency;
    @NotBlank(message = "Status is required")
    private String status;
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;
    @Override
    public String getEventId() {
        return transactionId;
    }

    @Override
    public String getEventType() {
        return "EVENT";
    }
    @Override
    public String toJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
