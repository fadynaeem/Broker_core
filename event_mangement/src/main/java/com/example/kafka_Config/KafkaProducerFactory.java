package com.example.kafka_Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * KafkaProducerFactory — infrastructure layer (ARCH TEAM).
 *
 * Extends KafkaBase to inherit bootstrapServers and baseProducerConfigs().
 * Produces two Spring beans consumed by all {@link BaseProducer} subclasses:
 *
 *   1. {@code ProducerFactory<String, String>} — thread-safe Kafka producer pool.
 *   2. {@code KafkaTemplate<String, String>}   — high-level send API.
 *
 * Business teams interact with these beans only through BaseProducer — never directly.
 *
 * Hierarchy position:
 *   KafkaBase
 *      └── KafkaProducerFactory  ← (you are here)
 *                └── [beans injected into] BaseProducer → (Business Producers)
 */
@Slf4j
@Configuration
public class KafkaProducerFactory extends KafkaBase {

    /**
     * Thread-safe producer factory backed by {@link #baseProducerConfigs()}.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        log.info("[KafkaProducerFactory] Creating ProducerFactory — broker: {}", bootstrapServers);
        return new DefaultKafkaProducerFactory<>(baseProducerConfigs());
    }

    /**
     * KafkaTemplate wrapping the producer factory.
     * This is the single point of contact for all send operations.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true); // enables Micrometer tracing if on classpath
        log.info("[KafkaProducerFactory] KafkaTemplate ready");
        return template;
    }
}
