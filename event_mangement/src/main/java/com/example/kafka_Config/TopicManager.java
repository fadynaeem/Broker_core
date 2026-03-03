package com.example.kafka_Config;

import com.example.shared.model.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.HashMap;
import java.util.Map;

/**
 * TopicManager — ARCH TEAM only (infrastructure layer).
 *
 * Extends KafkaBase to inherit bootstrapServers.
 * Owns all topic definitions: partitions, replication, and compaction policies.
 *
 * Rules:
 *  - Business teams MUST NOT add topics here directly.
 *  - To add a topic: add it to the {@link Channel} enum → a bean is auto-created below.
 *  - All topics follow the naming convention defined in {@link Channel}.
 *
 * Hierarchy position:
 *   KafkaBase
 *      └── TopicManager  ← (you are here)
 */
@Slf4j
@Configuration
public class TopicManager extends KafkaBase {

    /**
     * KafkaAdmin — connects to the broker so Spring can auto-create topics on startup.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        log.info("[TopicManager] KafkaAdmin initialised — broker: {}", bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Topic Definitions — add one @Bean per Channel entry
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Topic for {@link Channel#PAYMENT} — high-throughput, 6 partitions.
     */
    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(Channel.PAYMENT.getTopicName())
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Topic for {@link Channel#EMAIL} — standard notification flow, 3 partitions.
     */
    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(Channel.EMAIL.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
