package com.pulsewire.core.backbone;

import com.pulsewire.core.model.MarketEvent;

import java.util.function.Consumer;

/**
 * Backbone consumer abstraction – implementations can be in-memory, Kafka, etc.
 */
public interface BackboneConsumer {

    /**
     * Subscribe to a topic and process events.
     *
     * @param topic    topic/stream name
     * @param handler  callback for each event
     */
    void subscribe(String topic, Consumer<MarketEvent> handler);

    /**
     * Unsubscribe from a topic.
     *
     * @param topic topic/stream name
     */
    void unsubscribe(String topic);
}
