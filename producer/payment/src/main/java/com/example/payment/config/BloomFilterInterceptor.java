package com.example.payment.config;

import com.bloomfilter.BloomFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class BloomFilterInterceptor implements HandlerInterceptor {

    @Autowired private BloomFilter bloomFilter;
    @Autowired private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    throws Exception {
        byte[] body = (request instanceof CachedBodyRequestWrapper w) ? w.getCachedBody() : request.getInputStream().readAllBytes();
        JsonNode json;
        try { json = objectMapper.readTree(body); }
        catch (Exception e) {
            return reject(response, HttpStatus.BAD_REQUEST, "{\"error\":\"Invalid JSON body\"}");
        }
        String userId     = json.path("userId").asText(null);
        String paymentKey = json.path("paymentKey").asText(null);
        if (userId == null || userId.isBlank())
            return reject(response, HttpStatus.BAD_REQUEST, "{\"error\":\"Missing userId in body\"}");
        if (paymentKey == null || paymentKey.isBlank())
            return reject(response, HttpStatus.BAD_REQUEST, "{\"error\":\"Missing paymentKey in body\"}");
        String bloomKey = paymentKey + ":" + userId;
        if (bloomFilter.isMarked(bloomKey)) {
            log.warn("Bloom: key='{}' flagged — blocked", bloomKey);
            return reject(response, HttpStatus.FORBIDDEN,
                    "{\"error\":\"Duplicate or flagged payment request\","
                    + "\"userId\":\"" + userId + "\","
                    + "\"paymentKey\":\"" + paymentKey + "\"}");
        }
        bloomFilter.mark(bloomKey);
        log.info("Bloom: key='{}' marked — allowed", bloomKey);
        return true;
    }
    private boolean reject(HttpServletResponse response, HttpStatus status, String body) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(body);
        return false;
    }
}
