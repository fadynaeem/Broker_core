package com.example.worker.adapter;

import com.example.shared.model.DeliveryResult;
import com.example.worker.config.StripeConfig;
import com.example.shared.event.PaymentEvent;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentAdapter implements DeliveryAdapter {
    private final StripeConfig config;
    private final StripeClient stripeClient;
    @Override
    public DeliveryResult deliver(PaymentEvent message) {
        try {
            String amountStr = message.getAmount();
            if (amountStr == null) {
                return DeliveryResult.builder()
                        .success(false)
                        .errorMessage("Payment amount not provided in template params")
                        .build();
            }
            long amountInCents = convertToCents(amountStr);
            String customerEmail = message.getUserEmail();
            String description = message.getDescription() != null ? message.getDescription() : "Payment";
            if (config.isMockMode() || "MOCK".equals(config.getApiKey())) {
                log.info("PAYMENT NOTIFICATION (MOCK MODE)");
                log.info("Customer: {}", customerEmail);
                log.info("Amount: {} cents ({} {})", amountInCents,
                         formatAmount(amountInCents), config.getCurrency().toUpperCase());
                log.info("Description: {}", description);
                log.info("Transaction: {}", message.getTransactionId());
                return DeliveryResult.builder()
                        .success(true)
                        .messageId("MOCK-PAYMENT-" + System.currentTimeMillis())
                        .build();
            }
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(message.getCurrency() != null ? message.getCurrency() : config.getCurrency())
                    .setDescription(description)
                    .setReceiptEmail(customerEmail)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();
            PaymentIntent paymentIntent = stripeClient.paymentIntents().create(params);
            log.info("Payment intent created successfully. ID: {}, Status: {}",
                     paymentIntent.getId(), paymentIntent.getStatus());
            return DeliveryResult.builder()
                    .success(true)
                    .messageId(paymentIntent.getId())
                    .httpStatusCode(200)
                    .build();
        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage(), e);
            return DeliveryResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .transientError(isTransientError(e))
                    .httpStatusCode(e.getStatusCode() != null ? e.getStatusCode() : 0)
                    .build();
        } catch (Exception e) {
            log.error("Failed to process payment", e);
            return DeliveryResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .transientError(false)
                    .build();
        }
    }
    private long convertToCents(String amount) {
        BigDecimal decimalAmount = new BigDecimal(amount);
        return decimalAmount.multiply(new BigDecimal("100")).longValue();
    }
    private String formatAmount(long amountInCents) {
        BigDecimal amount = new BigDecimal(amountInCents).divide(new BigDecimal("100"));
        return amount.toPlainString();
    }
    private boolean isTransientError(StripeException e) {
        return e.getStatusCode() != null && 
               (e.getStatusCode() >= 500 || e.getStatusCode() == 429);
    }
}
