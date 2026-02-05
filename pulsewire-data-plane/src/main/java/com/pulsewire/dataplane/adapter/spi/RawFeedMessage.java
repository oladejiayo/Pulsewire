package com.pulsewire.dataplane.adapter.spi;

import java.time.Instant;

/**
 * Immutable record representing a raw message received from a feed.
 * 
 * <p>This is the transport-agnostic data structure passed to
 * {@link FeedEventHandler#onMessage}. Downstream stages process this
 * without knowing whether it came from TCP, UDP, WebSocket, or a vendor SDK.
 * 
 * <p><b>Design Decision (ADR-002):</b> Raw bytes preserve the original
 * message format (FIX, ITCH, JSON, protobuf, etc.) allowing the normalizer
 * to apply appropriate parsing based on feed configuration.
 * 
 * <p><b>Performance Note:</b> Uses primitive {@code long} for sequence number
 * to avoid boxing overhead in high-throughput scenarios.
 * 
 * @param payload The raw bytes of the message as received from the transport.
 *                May be the complete message or a framed portion depending on
 *                the transport's framing semantics.
 * @param receiveTimestamp The instant when the message was received by the adapter.
 *                         Uses {@link Instant} for nanosecond precision.
 *                         Critical for latency measurement and SLO tracking.
 * @param sequenceNumber The sequence number from the feed source (if available)
 *                       or an adapter-assigned sequence. Used for gap detection
 *                       and message ordering. Value of -1 indicates no sequence.
 */
public record RawFeedMessage(
        byte[] payload,
        Instant receiveTimestamp,
        long sequenceNumber
) {
    
    /**
     * Compact constructor for validation.
     */
    public RawFeedMessage {
        // Payload can be empty but not null
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (receiveTimestamp == null) {
            throw new IllegalArgumentException("Receive timestamp cannot be null");
        }
    }
    
    /**
     * Factory method for messages without a known sequence number.
     * 
     * @param payload the raw message bytes
     * @param receiveTimestamp when the message was received
     * @return a new RawFeedMessage with sequence number -1
     */
    public static RawFeedMessage withoutSequence(byte[] payload, Instant receiveTimestamp) {
        return new RawFeedMessage(payload, receiveTimestamp, -1L);
    }
    
    /**
     * @return true if this message has a valid sequence number
     */
    public boolean hasSequenceNumber() {
        return sequenceNumber >= 0;
    }
}
