package com.pulsewire.dataplane.adapter.spi;

/**
 * Callback interface for receiving events from a {@link FeedAdapter}.
 * 
 * <p>Implementations of this interface are passed to {@link FeedAdapter#connect}
 * to receive lifecycle events and messages. This separates the adapter's
 * connection management from message processing logic.
 * 
 * <p><b>Design Decision (ADR-003):</b> Separating lifecycle from message handling
 * enables cleaner composition. The same handler can be shared across adapters
 * or decorated with logging, metrics, or circuit breaker logic.
 * 
 * <p><b>Threading Model:</b> Callbacks are invoked on the adapter's I/O thread.
 * Implementations MUST be non-blocking. Heavy processing should be offloaded
 * to a separate executor to avoid blocking the adapter's message loop.
 * 
 * <p><b>Error Handling:</b> Exceptions thrown by callback methods may cause
 * the adapter to disconnect. Implementations should catch and handle exceptions
 * internally where recovery is possible.
 * 
 * @see FeedAdapter#connect(FeedEventHandler)
 */
public interface FeedEventHandler {
    
    /**
     * Called when the adapter successfully establishes a connection.
     * 
     * <p>This callback indicates the transport layer is connected and the
     * adapter is ready to receive messages. It does NOT guarantee the
     * application-level session is fully established (e.g., FIX logon).
     * 
     * @param adapterId the unique identifier of the adapter that connected
     */
    void onConnected(String adapterId);
    
    /**
     * Called when the adapter's connection is closed.
     * 
     * <p>This may be due to:
     * <ul>
     *   <li>Intentional disconnect via {@link FeedAdapter#disconnect()}</li>
     *   <li>Remote server closing the connection</li>
     *   <li>Network failure</li>
     *   <li>Protocol-level disconnect (e.g., FIX logout)</li>
     * </ul>
     * 
     * @param adapterId the unique identifier of the adapter that disconnected
     * @param reason a human-readable description of why the disconnect occurred
     */
    void onDisconnected(String adapterId, String reason);
    
    /**
     * Called when a message is received from the feed.
     * 
     * <p>The message is transport-agnostic - the same {@link RawFeedMessage}
     * structure is used regardless of whether data arrived via TCP, UDP,
     * WebSocket, or vendor SDK.
     * 
     * <p><b>Performance Critical:</b> This method is on the hot path.
     * Implementations MUST return quickly. Avoid:
     * <ul>
     *   <li>Blocking I/O</li>
     *   <li>Synchronization on contended locks</li>
     *   <li>Heavy allocations</li>
     * </ul>
     * 
     * @param adapterId the unique identifier of the adapter
     * @param message the raw feed message with payload and metadata
     */
    void onMessage(String adapterId, RawFeedMessage message);
    
    /**
     * Called when an error occurs during connection or message processing.
     * 
     * <p>Error types may include:
     * <ul>
     *   <li>Connection failures (IOException)</li>
     *   <li>Protocol errors (malformed messages)</li>
     *   <li>Authentication failures</li>
     *   <li>Rate limiting or quota exceeded</li>
     * </ul>
     * 
     * <p>The adapter may or may not remain connected after an error.
     * Check {@link FeedAdapter#isConnected()} if connection state is needed.
     * 
     * @param adapterId the unique identifier of the adapter
     * @param error the exception that occurred
     */
    void onError(String adapterId, Throwable error);
    
    /**
     * Called when a heartbeat response is not received within the expected timeout.
     * 
     * <p>This indicates the connection may be stale. The adapter typically
     * remains connected to allow recovery, but the orchestrator should
     * consider initiating a reconnection if the issue persists.
     * 
     * @param adapterId the unique identifier of the adapter
     */
    void onHeartbeatTimeout(String adapterId);
}
