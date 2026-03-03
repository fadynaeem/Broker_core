package com.example.kafka_Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Slf4j
@Configuration
public class KafkaProducerFactory extends KafkaBase {
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        log.info("[KafkaProducerFactory] Creating ProducerFactory — broker: {}", bootstrapServers);
        return new DefaultKafkaProducerFactory<>(baseProducerConfigs());
    }
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true); // enables Micrometer tracing if on classpath
        log.info("[KafkaProducerFactory] KafkaTemplate ready");
        return template;
    }
}
