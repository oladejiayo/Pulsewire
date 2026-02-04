package com.pulsewire.core.model;

import java.time.Instant;

/**
 * Canonical market event envelope.
 */
public record MarketEvent(
        String eventId,
        String instrumentId,
        EventType eventType,
        Instant exchangeTimestamp,
        Instant receiveTimestamp,
        Instant publishTimestamp,
        int schemaVersion,
        Object payload) {

    public enum EventType {
        TRADE,
        QUOTE,
        BOOK_SNAPSHOT,
        BOOK_DELTA,
        STATUS
    }
}
