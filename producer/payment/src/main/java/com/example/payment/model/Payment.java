package com.example.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @Column(name = "transaction_id", nullable = false, length = 64)
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    @Column(name = "user_id", nullable = false, length = 128)
    @NotBlank(message = "User ID is required")
    private String userId;
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 16)
    @NotBlank(message = "Currency is required")
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @NotNull(message = "Payment Status is required")
    private PaymentStatus status;
    @Column(name = "payment_method", nullable = false, length = 64)
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    @Column(name = "reference_id", length = 128)
    private String referenceId;
    @Column(name = "description", length = 512)
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "failure_reason", length = 512)
    private String failureReason;
}
