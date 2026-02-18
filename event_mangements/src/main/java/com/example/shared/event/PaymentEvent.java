package com.example.shared.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements ProducerEvent {
    private String transactionId;
    private String userEmail;
    private String amount;
    private String currency;
    private String description;
    private LocalDateTime createdAt;

    @Override
    public String getEventId() {
        return transactionId;
    }

    @Override
    public String getEventType() {
        return "PAYMENT";
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
