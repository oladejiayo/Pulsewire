package com.pulsewire.dataplane.normalizer;

import com.pulsewire.core.backbone.BackboneConsumer;
import com.pulsewire.core.backbone.BackbonePublisher;
import com.pulsewire.core.model.MarketEvent;
import com.pulsewire.core.model.MarketEvent.EventType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Normalizer service that consumes raw events, validates them,
 * and publishes canonical events.
 */
@Service
public class NormalizerService {

    private static final Logger log = LoggerFactory.getLogger(NormalizerService.class);

    private static final String RAW_TRADES_TOPIC = "raw.trades";
    private static final String RAW_QUOTES_TOPIC = "raw.quotes";
    private static final String CANONICAL_TOPIC = "canonical.events";

    private final BackboneConsumer consumer;
    private final BackbonePublisher publisher;
    private final AtomicLong normalizedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    @Value("${pulsewire.normalizer.schema-version:1}")
    private int schemaVersion;

    public NormalizerService(BackboneConsumer consumer, BackbonePublisher publisher) {
        this.consumer = consumer;
        this.publisher = publisher;
    }

    @PostConstruct
    public void start() {
        consumer.subscribe(RAW_TRADES_TOPIC, this::normalizeEvent);
        consumer.subscribe(RAW_QUOTES_TOPIC, this::normalizeEvent);
        log.info("NormalizerService started, subscribing to raw topics");
    }

    @PreDestroy
    public void stop() {
        consumer.unsubscribe(RAW_TRADES_TOPIC);
        consumer.unsubscribe(RAW_QUOTES_TOPIC);
        log.info("NormalizerService stopped");
    }

    private void normalizeEvent(MarketEvent rawEvent) {
        try {
            // Validate event
            if (!validate(rawEvent)) {
                errorCount.incrementAndGet();
                log.warn("Validation failed for event: {}", rawEvent.eventId());
                return;
            }

            // Create canonical event with publish timestamp
            MarketEvent canonical = new MarketEvent(
                    rawEvent.eventId(),
                    rawEvent.instrumentId(),
                    rawEvent.eventType(),
                    rawEvent.exchangeTimestamp(),
                    rawEvent.receiveTimestamp(),
                    Instant.now(),  // publish timestamp
                    schemaVersion,
                    rawEvent.payload()
            );

            // Publish to canonical topic
            publisher.publish(CANONICAL_TOPIC, canonical.instrumentId(), canonical);

            long count = normalizedCount.incrementAndGet();
            if (count % 1000 == 0) {
                log.info("Normalized {} events, {} errors", count, errorCount.get());
            }

        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("Error normalizing event: {}", rawEvent.eventId(), e);
        }
    }

    private boolean validate(MarketEvent event) {
        if (event == null) return false;
        if (event.eventId() == null || event.eventId().isBlank()) return false;
        if (event.instrumentId() == null || event.instrumentId().isBlank()) return false;
        if (event.eventType() == null) return false;
        if (event.payload() == null) return false;
        return true;
    }

    public long getNormalizedCount() {
        return normalizedCount.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }
}
