package com.example.kafka_architecture;

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
@Slf4j
@Configuration
public class TopicManager extends KafkaBase {
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        log.info("[TopicManager] KafkaAdmin initialised — broker: {}", bootstrapServers);
        return new KafkaAdmin(configs);
    }
    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(Channel.PAYMENT.getTopicName())
                .partitions(6)
                .replicas(1)
                .compact()
                .build();
    }
    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(Channel.EMAIL.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
