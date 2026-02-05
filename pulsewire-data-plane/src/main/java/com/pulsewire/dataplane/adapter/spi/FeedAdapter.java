package com.pulsewire.dataplane.adapter.spi;

/**
 * Service Provider Interface (SPI) for market data feed adapters.
 * 
 * <p>This interface defines the contract that all feed adapters must implement
 * to integrate with the PulseWire ingestion pipeline. It provides a consistent
 * abstraction over various transport protocols and vendor-specific APIs.
 * 
 * <h2>Acceptance Criteria Coverage</h2>
 * <ul>
 *   <li>AC1: Covers connect, disconnect, heartbeat, message receive (via handler), and error callbacks</li>
 *   <li>AC2: Supports multiple transport types via {@link TransportType} without leaking details</li>
 * </ul>
 * 
 * <h2>Lifecycle</h2>
 * <pre>
 *     DISCONNECTED ──connect()──▶ CONNECTING ──onConnected──▶ CONNECTED
 *          ▲                                                      │
 *          └──────────────────── disconnect()/error ◀─────────────┘
 * </pre>
 * 
 * <h2>Design Decisions</h2>
 * <ul>
 *   <li><b>ADR-001:</b> Callback-based SPI over polling for lower latency</li>
 *   <li><b>ADR-002:</b> Transport type is metadata, not behavioral interface</li>
 *   <li><b>ADR-003:</b> Separate lifecycle (FeedAdapter) from events (FeedEventHandler)</li>
 * </ul>
 * 
 * <h2>Threading</h2>
 * <p>Adapters manage their own I/O threads. Callbacks on {@link FeedEventHandler}
 * are invoked from the adapter's thread. Handlers must be thread-safe if shared.
 * 
 * <h2>Example Implementation</h2>
 * <pre>{@code
 * public class ExchangeXFeedAdapter implements FeedAdapter {
 *     private final String id = "exchange-x-" + UUID.randomUUID();
 *     
 *     @Override
 *     public void connect(FeedEventHandler handler) {
 *         // Establish connection, then:
 *         handler.onConnected(id);
 *     }
 *     
 *     @Override
 *     public TransportType getTransportType() {
 *         return TransportType.TCP;
 *     }
 *     // ... other methods
 * }
 * }</pre>
 * 
 * @see FeedEventHandler for event callbacks
 * @see TransportType for supported transports
 * @see RawFeedMessage for the transport-agnostic message format
 */
public interface FeedAdapter {
    
    /**
     * Returns the unique identifier for this adapter instance.
     * 
     * <p>The ID is used for:
     * <ul>
     *   <li>Logging and tracing</li>
     *   <li>Metrics tagging</li>
     *   <li>Lifecycle management</li>
     *   <li>Callback correlation in {@link FeedEventHandler}</li>
     * </ul>
     * 
     * <p>IDs should be stable for the lifetime of the adapter instance
     * and unique across all adapters in the system.
     * 
     * @return a non-null, non-empty unique identifier
     */
    String getId();
    
    /**
     * Returns the transport type used by this adapter.
     * 
     * <p>This is metadata for configuration, monitoring, and metrics.
     * It does NOT expose transport-specific behavior to callers.
     * 
     * @return the transport type, never null
     */
    TransportType getTransportType();
    
    /**
     * Initiates a connection to the feed source.
     * 
     * <p>This method is asynchronous. The actual connection may complete
     * after this method returns. Use {@link FeedEventHandler#onConnected}
     * to be notified when the connection is established.
     * 
     * <p>If the adapter is already connected or connecting, this method
     * may throw {@link IllegalStateException} or be a no-op depending
     * on the implementation.
     * 
     * @param handler the callback handler for connection events and messages
     * @throws IllegalArgumentException if handler is null
     * @throws IllegalStateException if adapter is already connected
     */
    void connect(FeedEventHandler handler);
    
    /**
     * Gracefully closes the connection to the feed source.
     * 
     * <p>This method initiates a graceful shutdown:
     * <ul>
     *   <li>Sends any required protocol-level disconnect messages</li>
     *   <li>Waits briefly for acknowledgment (implementation-dependent)</li>
     *   <li>Closes the underlying transport</li>
     *   <li>Invokes {@link FeedEventHandler#onDisconnected}</li>
     * </ul>
     * 
     * <p>If the adapter is not connected, this method is a no-op.
     */
    void disconnect();
    
    /**
     * Checks whether the adapter is currently connected.
     * 
     * <p>Note: This reflects the adapter's view of connection state.
     * The actual network connection may have dropped without the
     * adapter detecting it yet (especially for TCP without heartbeats).
     * 
     * @return true if the adapter believes it is connected
     */
    boolean isConnected();
    
    /**
     * Sends a heartbeat to maintain connection liveness.
     * 
     * <p>The heartbeat mechanism is transport-specific:
     * <ul>
     *   <li>TCP: May send a FIX Heartbeat or vendor-specific ping</li>
     *   <li>WebSocket: Sends a ping frame</li>
     *   <li>UDP: May send a presence message to the multicast group</li>
     *   <li>Vendor SDK: Delegates to the SDK's keepalive mechanism</li>
     * </ul>
     * 
     * <p>If no heartbeat response is received within the expected timeout,
     * {@link FeedEventHandler#onHeartbeatTimeout} is invoked.
     * 
     * @throws IllegalStateException if not connected
     */
    void sendHeartbeat();
}
