package com.pulsewire.core.backbone;

import com.pulsewire.core.model.MarketEvent;

/**
 * Backbone publisher abstraction – implementations can be in-memory, Kafka, etc.
 */
public interface BackbonePublisher {

    /**
     * Publish event to a topic.
     *
     * @param topic  destination topic/stream name
     * @param key    partition key (e.g., instrumentId)
     * @param event  event to publish
     */
    void publish(String topic, String key, MarketEvent event);
}
