package com.pulsewire.core.backbone;

import com.pulsewire.core.model.MarketEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple in-memory backbone for local development and testing.
 */
public class InMemoryBackbone implements BackbonePublisher, BackboneConsumer {

    private final Map<String, List<Consumer<MarketEvent>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(String topic, String key, MarketEvent event) {
        List<Consumer<MarketEvent>> handlers = subscribers.get(topic);
        if (handlers != null) {
            for (Consumer<MarketEvent> handler : handlers) {
                handler.accept(event);
            }
        }
    }

    @Override
    public void subscribe(String topic, Consumer<MarketEvent> handler) {
        subscribers.computeIfAbsent(topic, t -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public void unsubscribe(String topic) {
        subscribers.remove(topic);
    }
}
