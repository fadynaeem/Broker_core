package com.example.payment.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String transactionId;
    private String status;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}
