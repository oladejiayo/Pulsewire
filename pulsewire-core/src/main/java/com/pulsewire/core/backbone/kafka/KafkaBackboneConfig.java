package com.pulsewire.core.backbone.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pulsewire.core.backbone.BackboneConsumer;
import com.pulsewire.core.backbone.BackbonePublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Kafka backbone.
 * Activated when 'pulsewire.backbone.type=kafka' is set.
 */
@Configuration
@ConditionalOnProperty(name = "pulsewire.backbone.type", havingValue = "kafka")
public class KafkaBackboneConfig {

    @Value("${pulsewire.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${pulsewire.kafka.consumer.group-id:pulsewire-consumers}")
    private String consumerGroupId;

    @Bean
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public BackbonePublisher kafkaBackbonePublisher(ObjectMapper kafkaObjectMapper) {
        return new KafkaBackbonePublisher(bootstrapServers, kafkaObjectMapper);
    }

    @Bean
    public BackboneConsumer kafkaBackboneConsumer(ObjectMapper kafkaObjectMapper) {
        return new KafkaBackboneConsumer(bootstrapServers, consumerGroupId, kafkaObjectMapper);
    }
}
