package com.pulsewire.core.backbone;

import com.pulsewire.core.model.MarketEvent;
import com.pulsewire.core.model.MarketEvent.EventType;
import com.pulsewire.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBackboneTest {

    private InMemoryBackbone backbone;

    @BeforeEach
    void setUp() {
        backbone = new InMemoryBackbone();
    }

    @Test
    void shouldPublishAndReceiveEvent() throws InterruptedException {
        String topic = "test.topic";
        List<MarketEvent> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        backbone.subscribe(topic, event -> {
            received.add(event);
            latch.countDown();
        });

        MarketEvent event = createTestEvent("AAPL");
        backbone.publish(topic, "AAPL", event);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals("AAPL", received.get(0).instrumentId());
    }

    @Test
    void shouldFanoutToMultipleSubscribers() throws InterruptedException {
        String topic = "fanout.topic";
        List<MarketEvent> received1 = new ArrayList<>();
        List<MarketEvent> received2 = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        backbone.subscribe(topic, event -> {
            received1.add(event);
            latch.countDown();
        });
        backbone.subscribe(topic, event -> {
            received2.add(event);
            latch.countDown();
        });

        MarketEvent event = createTestEvent("GOOG");
        backbone.publish(topic, "GOOG", event);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
    }

    @Test
    void shouldNotReceiveAfterUnsubscribe() {
        String topic = "unsub.topic";
        List<MarketEvent> received = new ArrayList<>();

        backbone.subscribe(topic, received::add);
        backbone.unsubscribe(topic);

        MarketEvent event = createTestEvent("MSFT");
        backbone.publish(topic, "MSFT", event);

        assertTrue(received.isEmpty());
    }

    @Test
    void shouldHandleNoSubscribers() {
        // Should not throw
        MarketEvent event = createTestEvent("TSLA");
        assertDoesNotThrow(() -> backbone.publish("no.subscribers", "TSLA", event));
    }

    private MarketEvent createTestEvent(String instrumentId) {
        Trade trade = new Trade(BigDecimal.valueOf(150.00), BigDecimal.valueOf(100), null);
        return new MarketEvent(
                UUID.randomUUID().toString(),
                instrumentId,
                EventType.TRADE,
                Instant.now(),
                Instant.now(),
                null,
                1,
                trade
        );
    }
}
