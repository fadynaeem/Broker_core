package com.example.kafka_Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaBase — root of the kafka_arch hierarchy.
 *
 * Responsibilities:
 *  - Holds the shared bootstrap-servers property
 *  - Exposes the common ObjectMapper bean (used by all producers)
 *  - Provides baseProducerConfigs() helper consumed by KafkaProducerFactory
 *
 * Hierarchy:
 *
 *       KafkaBase
 *       /       \
 * TopicManager  KafkaProducerFactory → BaseProducer → (Business Producers)
 */
@Configuration
public abstract class KafkaBase {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    protected String bootstrapServers;

    /**
     * Shared ObjectMapper — registered once, reused across all producers.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Base producer configuration map shared by KafkaProducerFactory.
     * Override in subclasses to add environment-specific tuning.
     */
    protected Map<String, Object> baseProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Durability: wait for all in-sync replicas
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // Retry on transient errors
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        // Throughput tuning
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        return props;
    }
}
