package com.pulsewire.dataplane.adapter.spi;

/**
 * Enumeration of supported transport types for feed adapters.
 * 
 * <p>This enum is used for configuration, metrics tagging, and operational
 * visibility. It does NOT expose transport-specific behavior to downstream
 * stages - the {@link RawFeedMessage} abstraction ensures transport agnosticism.
 * 
 * <p><b>Design Decision (ADR-002):</b> Transport type is metadata only.
 * Downstream normalizers and processors receive the same {@link RawFeedMessage}
 * regardless of transport, enabling consistent processing logic.
 * 
 * @see FeedAdapter#getTransportType()
 */
public enum TransportType {
    
    /**
     * TCP socket connection.
     * <p>Common for exchange direct feeds and FIX protocol connections.
     * Characteristics: reliable, ordered, connection-oriented.
     */
    TCP,
    
    /**
     * UDP socket connection (often multicast).
     * <p>Common for high-throughput market data distribution.
     * Characteristics: low latency, no connection overhead, may have gaps.
     */
    UDP,
    
    /**
     * WebSocket connection.
     * <p>Common for browser-based feeds and modern REST/WebSocket hybrid APIs.
     * Characteristics: full-duplex, HTTP-upgrade based, firewall-friendly.
     */
    WEBSOCKET,
    
    /**
     * Proprietary vendor SDK.
     * <p>Used for data providers like Bloomberg, Reuters, ICE that provide
     * their own client libraries with proprietary protocols.
     * Characteristics: vendor-managed, may include built-in reconnection.
     */
    VENDOR_SDK
}
