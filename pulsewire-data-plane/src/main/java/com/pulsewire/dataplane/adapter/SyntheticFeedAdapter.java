package com.pulsewire.dataplane.adapter;

import com.pulsewire.core.backbone.BackbonePublisher;
import com.pulsewire.core.model.MarketEvent;
import com.pulsewire.core.model.MarketEvent.EventType;
import com.pulsewire.core.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Synthetic feed adapter that emits fake trade events for demo and testing.
 */
@Component
public class SyntheticFeedAdapter implements FeedAdapter {

    private static final Logger log = LoggerFactory.getLogger(SyntheticFeedAdapter.class);

    private static final List<String> INSTRUMENTS = List.of("AAPL", "GOOG", "MSFT", "AMZN", "TSLA");
    private static final String RAW_TOPIC = "raw.trades";

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong sequenceNumber = new AtomicLong(0);
    private final Random random = new Random();

    private ScheduledExecutorService executor;
    private BackbonePublisher publisher;

    @Override
    public void start(BackbonePublisher publisher) {
        if (running.compareAndSet(false, true)) {
            this.publisher = publisher;
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "synthetic-feed");
                t.setDaemon(true);
                return t;
            });
            executor.scheduleAtFixedRate(this::emitTrade, 0, 100, TimeUnit.MILLISECONDS);
            log.info("SyntheticFeedAdapter started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (executor != null) {
                executor.shutdownNow();
            }
            log.info("SyntheticFeedAdapter stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void emitTrade() {
        try {
            String instrument = INSTRUMENTS.get(random.nextInt(INSTRUMENTS.size()));
            BigDecimal price = BigDecimal.valueOf(100 + random.nextDouble() * 50).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal size = BigDecimal.valueOf(random.nextInt(1000) + 1);

            Trade trade = new Trade(price, size, null);
            Instant now = Instant.now();
            MarketEvent event = new MarketEvent(
                    UUID.randomUUID().toString(),
                    instrument,
                    EventType.TRADE,
                    now,
                    now,
                    null,
                    1,
                    trade);

            publisher.publish(RAW_TOPIC, instrument, event);
            long seq = sequenceNumber.incrementAndGet();
            if (seq % 100 == 0) {
                log.debug("Emitted {} events so far", seq);
            }
        } catch (Exception e) {
            log.error("Error emitting trade", e);
        }
    }
}
