package com.pulsewire.core.backbone.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsewire.core.backbone.BackbonePublisher;
import com.pulsewire.core.model.MarketEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka-backed implementation of BackbonePublisher.
 * Publishes MarketEvents to Kafka topics using the instrument ID as partition key.
 */
public class KafkaBackbonePublisher implements BackbonePublisher, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(KafkaBackbonePublisher.class);

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public KafkaBackbonePublisher(String bootstrapServers) {
        this(bootstrapServers, new ObjectMapper());
    }

    public KafkaBackbonePublisher(String bootstrapServers, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        this.producer = new KafkaProducer<>(props);
        log.info("KafkaBackbonePublisher initialized with bootstrap servers: {}", bootstrapServers);
    }

    @Override
    public void publish(String topic, String key, MarketEvent event) {
        if (closed.get()) {
            throw new IllegalStateException("Publisher is closed");
        }

        try {
            String value = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to publish event to topic {} with key {}", topic, key, exception);
                } else {
                    log.debug("Published event to topic {} partition {} offset {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for topic {} with key {}", topic, key, e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * Flush pending messages to Kafka.
     */
    public void flush() {
        producer.flush();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            producer.close();
            log.info("KafkaBackbonePublisher closed");
        }
    }
}
