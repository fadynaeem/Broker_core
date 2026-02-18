package com.example.worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {
    private String apiKey = "MOCK";
    private boolean mockMode = true;
    private String currency = "usd";
}
