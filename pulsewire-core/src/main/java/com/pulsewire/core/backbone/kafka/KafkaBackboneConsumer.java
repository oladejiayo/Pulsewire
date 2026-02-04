package com.pulsewire.core.backbone.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsewire.core.backbone.BackboneConsumer;
import com.pulsewire.core.model.MarketEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Kafka-backed implementation of BackboneConsumer.
 * Consumes MarketEvents from Kafka topics and dispatches to registered handlers.
 */
public class KafkaBackboneConsumer implements BackboneConsumer, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(KafkaBackboneConsumer.class);

    private final String bootstrapServers;
    private final String groupId;
    private final ObjectMapper objectMapper;
    private final Map<String, ConsumerThread> consumerThreads = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public KafkaBackboneConsumer(String bootstrapServers, String groupId) {
        this(bootstrapServers, groupId, new ObjectMapper());
    }

    public KafkaBackboneConsumer(String bootstrapServers, String groupId, ObjectMapper objectMapper) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.objectMapper = objectMapper;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "kafka-consumer");
            t.setDaemon(true);
            return t;
        });
        log.info("KafkaBackboneConsumer initialized with bootstrap servers: {}, groupId: {}", bootstrapServers, groupId);
    }

    @Override
    public void subscribe(String topic, Consumer<MarketEvent> handler) {
        if (closed.get()) {
            throw new IllegalStateException("Consumer is closed");
        }

        ConsumerThread existing = consumerThreads.putIfAbsent(topic, new ConsumerThread(topic, handler));
        if (existing == null) {
            executor.submit(consumerThreads.get(topic));
            log.info("Subscribed to topic: {}", topic);
        } else {
            log.warn("Already subscribed to topic: {}", topic);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        ConsumerThread thread = consumerThreads.remove(topic);
        if (thread != null) {
            thread.stop();
            log.info("Unsubscribed from topic: {}", topic);
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            consumerThreads.values().forEach(ConsumerThread::stop);
            consumerThreads.clear();
            executor.shutdownNow();
            log.info("KafkaBackboneConsumer closed");
        }
    }

    private class ConsumerThread implements Runnable {
        private final String topic;
        private final Consumer<MarketEvent> handler;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private KafkaConsumer<String, String> consumer;

        ConsumerThread(String topic, Consumer<MarketEvent> handler) {
            this.topic = topic;
            this.handler = handler;
        }

        void stop() {
            running.set(false);
            if (consumer != null) {
                consumer.wakeup();
            }
        }

        @Override
        public void run() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(topic));

            log.info("Consumer thread started for topic: {}", topic);

            try {
                while (running.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    records.forEach(record -> {
                        try {
                            MarketEvent event = objectMapper.readValue(record.value(), MarketEvent.class);
                            handler.accept(event);
                        } catch (Exception e) {
                            log.error("Failed to process record from topic {} partition {} offset {}",
                                    record.topic(), record.partition(), record.offset(), e);
                        }
                    });
                }
            } catch (org.apache.kafka.common.errors.WakeupException e) {
                if (running.get()) {
                    throw e;
                }
            } finally {
                consumer.close();
                log.info("Consumer thread stopped for topic: {}", topic);
            }
        }
    }
}
