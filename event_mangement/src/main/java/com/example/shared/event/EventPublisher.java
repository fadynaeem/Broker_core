package com.example.shared.event;
public interface EventPublisher {
    /**
     * Publish an event to Kafka
     * @param event The event to publish
     * @param topic The Kafka topic
     * @return The event ID
     */
    String publishEvent(ProducerEvent event, String topic);
    
    /**
     * Publish an event with a specific key
     * @param event The event to publish
     * @param topic The Kafka topic
     * @param key The partition key
     * @return The event ID
     */
    String publishEventWithKey(ProducerEvent event, String topic, String key);
}
