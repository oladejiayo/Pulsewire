package com.pulsewire.dataplane.adapter.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Example implementation of the {@link FeedAdapter} SPI for testing and demonstration.
 * 
 * <p>This adapter generates synthetic market data events at a configurable rate,
 * simulating what a real feed adapter would do when connected to an exchange or
 * data vendor.
 * 
 * <h2>Acceptance Criteria Coverage</h2>
 * <ul>
 *   <li>AC3: Example implementation that compiles and can be wired into pipeline</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Implements all {@link FeedAdapter} lifecycle methods</li>
 *   <li>Generates random trade events for demonstration symbols</li>
 *   <li>Assigns monotonically increasing sequence numbers</li>
 *   <li>Thread-safe start/stop operations</li>
 *   <li>Proper callback invocation on connect/disconnect/message</li>
 * </ul>
 * 
 * <h2>Threading Model</h2>
 * <p>Uses a single-threaded {@link ScheduledExecutorService} for message generation.
 * Callbacks are invoked on the executor thread. Handlers must be non-blocking.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * SyntheticFeedAdapterV2 adapter = new SyntheticFeedAdapterV2();
 * adapter.connect(new MyFeedEventHandler());
 * // ... receive messages via handler.onMessage()
 * adapter.disconnect();
 * }</pre>
 * 
 * @see FeedAdapter
 * @see FeedEventHandler
 */
public class SyntheticFeedAdapterV2 implements FeedAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(SyntheticFeedAdapterV2.class);
    
    /**
     * Demo symbols for synthetic data generation.
     * In production, these would come from configuration or subscription state.
     */
    private static final List<String> DEMO_SYMBOLS = List.of(
        "AAPL", "GOOGL", "MSFT", "AMZN", "META", "NVDA", "TSLA", "JPM"
    );
    
    /**
     * Interval between synthetic message emissions.
     * Low for demo purposes; production adapters would be driven by actual feed rate.
     */
    private static final long EMIT_INTERVAL_MS = 100;
    
    // Unique identifier for this adapter instance
    private final String id;
    
    // Thread-safe state management
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicLong sequenceNumber = new AtomicLong(0);
    private final AtomicReference<FeedEventHandler> handlerRef = new AtomicReference<>();
    
    // Message generation
    private final Random random = new Random();
    private volatile ScheduledExecutorService executor;
    
    /**
     * Creates a new synthetic feed adapter with a unique ID.
     */
    public SyntheticFeedAdapterV2() {
        this.id = "synthetic-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Creates a synthetic feed adapter with a custom ID.
     * Useful for testing when a specific ID is needed.
     * 
     * @param customId the ID to use for this adapter
     */
    public SyntheticFeedAdapterV2(String customId) {
        this.id = customId;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    /**
     * Returns VENDOR_SDK as this is an internal/synthetic feed,
     * not a real network transport.
     */
    @Override
    public TransportType getTransportType() {
        return TransportType.VENDOR_SDK;
    }
    
    @Override
    public void connect(FeedEventHandler handler) {
        // Validate handler
        if (handler == null) {
            throw new IllegalArgumentException("FeedEventHandler cannot be null");
        }
        
        // Ensure not already connected (atomic check-and-set)
        if (!connected.compareAndSet(false, true)) {
            throw new IllegalStateException("Adapter is already connected");
        }
        
        handlerRef.set(handler);
        
        // Create executor for message generation
        // Daemon thread so JVM can exit cleanly
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "synthetic-feed-" + id);
            t.setDaemon(true);
            return t;
        });
        
        // Notify connected (on executor thread for consistency)
        executor.execute(() -> {
            log.info("SyntheticFeedAdapterV2 [{}] connected", id);
            handler.onConnected(id);
        });
        
        // Schedule message generation
        executor.scheduleAtFixedRate(
            this::emitSyntheticMessage,
            EMIT_INTERVAL_MS,  // initial delay
            EMIT_INTERVAL_MS,  // period
            TimeUnit.MILLISECONDS
        );
    }
    
    @Override
    public void disconnect() {
        // Only disconnect if currently connected
        if (!connected.compareAndSet(true, false)) {
            log.debug("SyntheticFeedAdapterV2 [{}] disconnect called but not connected", id);
            return;
        }
        
        // Shutdown executor
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        
        // Notify disconnected
        FeedEventHandler handler = handlerRef.getAndSet(null);
        if (handler != null) {
            log.info("SyntheticFeedAdapterV2 [{}] disconnected", id);
            handler.onDisconnected(id, "Intentional disconnect");
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected.get();
    }
    
    @Override
    public void sendHeartbeat() {
        if (!connected.get()) {
            throw new IllegalStateException("Cannot send heartbeat: adapter not connected");
        }
        
        // Synthetic adapter doesn't need real heartbeats,
        // but we log for demonstration
        log.debug("SyntheticFeedAdapterV2 [{}] heartbeat sent", id);
    }
    
    /**
     * Generates and emits a synthetic market data message.
     * 
     * <p>This method is invoked periodically by the scheduled executor.
     * It creates a fake trade event with random data and invokes
     * the handler's onMessage callback.
     */
    private void emitSyntheticMessage() {
        FeedEventHandler handler = handlerRef.get();
        if (handler == null || !connected.get()) {
            return;
        }
        
        try {
            // Select random symbol
            String symbol = DEMO_SYMBOLS.get(random.nextInt(DEMO_SYMBOLS.size()));
            
            // Generate synthetic trade data as JSON
            // In production, this would be the raw bytes from the feed
            double price = 100.0 + (random.nextDouble() * 100.0);
            int quantity = random.nextInt(1000) + 1;
            String side = random.nextBoolean() ? "BUY" : "SELL";
            
            String json = String.format(
                "{\"type\":\"TRADE\",\"symbol\":\"%s\",\"price\":%.2f,\"qty\":%d,\"side\":\"%s\"}",
                symbol, price, quantity, side
            );
            
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            long seq = sequenceNumber.incrementAndGet();
            Instant now = Instant.now();
            
            RawFeedMessage message = new RawFeedMessage(payload, now, seq);
            handler.onMessage(id, message);
            
            if (seq % 100 == 0) {
                log.debug("SyntheticFeedAdapterV2 [{}] emitted {} messages", id, seq);
            }
        } catch (Exception e) {
            log.error("SyntheticFeedAdapterV2 [{}] error emitting message", id, e);
            FeedEventHandler h = handlerRef.get();
            if (h != null) {
                h.onError(id, e);
            }
        }
    }
}
