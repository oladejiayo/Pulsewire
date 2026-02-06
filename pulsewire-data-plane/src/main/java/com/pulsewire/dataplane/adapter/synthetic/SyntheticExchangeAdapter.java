package com.pulsewire.dataplane.adapter.synthetic;

import com.pulsewire.dataplane.adapter.spi.FeedAdapter;
import com.pulsewire.dataplane.adapter.spi.FeedEventHandler;
import com.pulsewire.dataplane.adapter.spi.RawFeedMessage;
import com.pulsewire.dataplane.adapter.spi.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Synthetic Exchange Feed Adapter that generates realistic trade and quote events.
 * 
 * <p>This adapter implements the {@link FeedAdapter} SPI to produce mock market data
 * for development and testing purposes, without requiring a connection to a real exchange.
 * 
 * <h2>Acceptance Criteria Coverage (US01-02)</h2>
 * <ul>
 *   <li><b>AC1:</b> Start/stop independently via configuration (enabled flag)</li>
 *   <li><b>AC2:</b> Emits trade and quote messages with realistic fields and sequence numbers</li>
 *   <li><b>AC3:</b> Configurable symbols, message rate, and burst patterns</li>
 * </ul>
 * 
 * <h2>Message Generation</h2>
 * <p>Generates two types of messages:
 * <ul>
 *   <li>{@link SyntheticTrade} - Executed transactions with price, quantity, side</li>
 *   <li>{@link SyntheticQuote} - Top-of-book bid/ask updates</li>
 * </ul>
 * 
 * <h2>Price Model</h2>
 * <p>Uses a random walk model where prices drift by small random amounts.
 * Each symbol maintains its own price state to produce realistic time series.
 * 
 * <h2>Burst Mode</h2>
 * <p>When enabled, periodically increases the message rate to simulate
 * market events like open, close, or news announcements.
 * 
 * <h2>Threading Model</h2>
 * <p>Uses a {@link ScheduledExecutorService} for message scheduling.
 * All callbacks are invoked on the executor thread. Handlers must be non-blocking.
 * 
 * @see SyntheticFeedConfig for configuration options
 * @see FeedAdapter for the SPI contract
 */
public class SyntheticExchangeAdapter implements FeedAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(SyntheticExchangeAdapter.class);
    
    // Adapter identity and configuration
    private final String id;
    private final SyntheticFeedConfig config;
    
    // Thread-safe state management
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicLong sequenceNumber = new AtomicLong(0);
    private final AtomicReference<FeedEventHandler> handlerRef = new AtomicReference<>();
    
    // Price state for each symbol (thread-safe map)
    private final Map<String, SymbolPriceState> priceStates = new ConcurrentHashMap<>();
    
    // Burst state
    private final AtomicBoolean inBurst = new AtomicBoolean(false);
    
    // Message generation (initialized on connect)
    private final Random random = new Random();
    private volatile ScheduledExecutorService executor;
    private volatile ScheduledFuture<?> messageTask;
    private volatile ScheduledFuture<?> burstTask;
    
    // Message type counter for trade/quote distribution
    private final AtomicLong messageCounter = new AtomicLong(0);
    
    /**
     * Creates a new synthetic exchange adapter with the given configuration.
     * 
     * @param config the adapter configuration
     */
    public SyntheticExchangeAdapter(SyntheticFeedConfig config) {
        this.id = "synthetic-exchange-" + UUID.randomUUID().toString().substring(0, 8);
        this.config = config;
        
        // Initialize price state for each configured symbol
        initializePriceStates();
    }
    
    /**
     * Initializes price states for all configured symbols with realistic base prices.
     */
    private void initializePriceStates() {
        // Base prices for common symbols (simplified - production would use reference data)
        Map<String, Double> basePrices = Map.of(
            "AAPL", 185.0,
            "GOOGL", 140.0,
            "MSFT", 375.0,
            "AMZN", 170.0,
            "META", 480.0,
            "NVDA", 850.0,
            "TSLA", 240.0,
            "JPM", 190.0
        );
        
        for (String symbol : config.symbols()) {
            double basePrice = basePrices.getOrDefault(symbol, 100.0);
            priceStates.put(symbol, new SymbolPriceState(symbol, basePrice, random));
        }
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public TransportType getTransportType() {
        // Synthetic feed is an internal/SDK-based feed, not a real network transport
        return TransportType.VENDOR_SDK;
    }
    
    @Override
    public void connect(FeedEventHandler handler) {
        // If disabled in config, do nothing (AC1: start/stop via configuration)
        if (!config.enabled()) {
            log.info("SyntheticExchangeAdapter [{}] is disabled in configuration, not starting", id);
            return;
        }
        
        if (handler == null) {
            throw new IllegalArgumentException("FeedEventHandler cannot be null");
        }
        
        // Atomic check-and-set to prevent double connection
        if (!connected.compareAndSet(false, true)) {
            throw new IllegalStateException("Adapter is already connected");
        }
        
        handlerRef.set(handler);
        
        // Reset sequence number on new connection
        sequenceNumber.set(0);
        messageCounter.set(0);
        
        // Create executor for message generation (daemon threads for clean shutdown)
        executor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "synthetic-exchange-" + id);
            t.setDaemon(true);
            return t;
        });
        
        // Notify connected
        executor.execute(() -> {
            log.info("SyntheticExchangeAdapter [{}] connected with config: symbols={}, rate={}/s, burst={}",
                    id, config.symbols(), config.messageRatePerSecond(), config.burstEnabled());
            handler.onConnected(id);
        });
        
        // Schedule message generation based on rate
        scheduleMessageGeneration();
        
        // Schedule burst mode if enabled
        if (config.burstEnabled()) {
            scheduleBurstMode();
        }
    }
    
    /**
     * Schedules the message generation task based on configured rate.
     */
    private void scheduleMessageGeneration() {
        // Calculate interval between messages
        // Rate is messages per second, so interval = 1000ms / rate
        long intervalMs = Math.max(1, 1000 / config.messageRatePerSecond());
        
        messageTask = executor.scheduleAtFixedRate(
            this::emitMessage,
            intervalMs,  // initial delay
            intervalMs,  // period (will be adjusted during burst)
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Schedules burst mode transitions.
     */
    private void scheduleBurstMode() {
        burstTask = executor.scheduleAtFixedRate(() -> {
            // Enter burst mode
            inBurst.set(true);
            log.debug("SyntheticExchangeAdapter [{}] entering burst mode", id);
            
            // Schedule exit from burst mode
            executor.schedule(() -> {
                inBurst.set(false);
                log.debug("SyntheticExchangeAdapter [{}] exiting burst mode", id);
            }, config.burstDurationMs(), TimeUnit.MILLISECONDS);
            
        }, config.burstIntervalMs(), config.burstIntervalMs(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Emits a single message (trade or quote) to the handler.
     */
    private void emitMessage() {
        FeedEventHandler handler = handlerRef.get();
        if (handler == null || !connected.get()) {
            return;
        }
        
        // During burst, emit multiple messages per tick
        int messagesToEmit = inBurst.get() ? config.burstMultiplier() : 1;
        
        for (int i = 0; i < messagesToEmit; i++) {
            emitSingleMessage(handler);
        }
    }
    
    /**
     * Emits a single message, choosing between trade and quote based on ratio.
     */
    private void emitSingleMessage(FeedEventHandler handler) {
        try {
            // Pick a random symbol
            String symbol = config.symbols().get(random.nextInt(config.symbols().size()));
            SymbolPriceState priceState = priceStates.get(symbol);
            
            // Decide message type based on trade/quote ratio
            // If ratio is 5, then 1 in 6 messages is a trade (1 trade per 5 quotes)
            long count = messageCounter.incrementAndGet();
            boolean isTrade = (count % (config.tradeToQuoteRatio() + 1)) == 0;
            
            byte[] payload;
            if (isTrade) {
                payload = generateTrade(symbol, priceState).toBytes();
            } else {
                payload = generateQuote(symbol, priceState).toBytes();
            }
            
            RawFeedMessage message = new RawFeedMessage(
                payload,
                Instant.now(),
                sequenceNumber.incrementAndGet()
            );
            
            handler.onMessage(id, message);
            
        } catch (Exception e) {
            log.error("Error emitting message in SyntheticExchangeAdapter [{}]", id, e);
            FeedEventHandler h = handlerRef.get();
            if (h != null) {
                h.onError(id, e);
            }
        }
    }
    
    /**
     * Generates a synthetic trade message.
     */
    private SyntheticTrade generateTrade(String symbol, SymbolPriceState priceState) {
        double price = priceState.nextPrice();
        return new SyntheticTrade(
            symbol,
            price,
            randomQuantity(),
            Instant.now(),
            "T" + UUID.randomUUID().toString().substring(0, 8),
            random.nextBoolean() ? TradeSide.BUY : TradeSide.SELL
        );
    }
    
    /**
     * Generates a synthetic quote message.
     */
    private SyntheticQuote generateQuote(String symbol, SymbolPriceState priceState) {
        double midPrice = priceState.nextPrice();
        double halfSpread = priceState.getHalfSpread();
        
        return new SyntheticQuote(
            symbol,
            midPrice - halfSpread,  // bid
            randomQuantity(),
            midPrice + halfSpread,  // ask
            randomQuantity(),
            Instant.now()
        );
    }
    
    /**
     * Generates a random quantity between 100 and 10000 (in round lots).
     */
    private long randomQuantity() {
        // Generate in round lots of 100
        return (random.nextInt(100) + 1) * 100L;
    }
    
    @Override
    public void disconnect() {
        if (!connected.compareAndSet(true, false)) {
            // Already disconnected or never connected
            return;
        }
        
        FeedEventHandler handler = handlerRef.getAndSet(null);
        
        // Cancel scheduled tasks
        if (messageTask != null) {
            messageTask.cancel(false);
        }
        if (burstTask != null) {
            burstTask.cancel(false);
        }
        
        // Shutdown executor
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
        
        // Notify disconnected
        if (handler != null) {
            log.info("SyntheticExchangeAdapter [{}] disconnected", id);
            handler.onDisconnected(id, "Disconnect requested");
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected.get();
    }
    
    @Override
    public void sendHeartbeat() {
        // Synthetic adapter doesn't require heartbeats (internal feed)
        // No-op implementation
        log.debug("SyntheticExchangeAdapter [{}] heartbeat (no-op)", id);
    }
    
    /**
     * Maintains price state for a single symbol using a random walk model.
     * 
     * <p>Thread-safe for concurrent access during message generation.
     */
    private static class SymbolPriceState {
        private final String symbol;
        private volatile double lastPrice;
        private final Random random;
        
        // Price drift parameters
        private static final double MAX_DRIFT_PERCENT = 0.001; // 0.1% max drift per tick
        private static final double SPREAD_PERCENT = 0.0002;   // 0.02% half-spread
        
        SymbolPriceState(String symbol, double initialPrice, Random random) {
            this.symbol = symbol;
            this.lastPrice = initialPrice;
            this.random = random;
        }
        
        /**
         * Gets the next price using random walk.
         * 
         * @return the new price
         */
        synchronized double nextPrice() {
            // Random drift: -MAX_DRIFT to +MAX_DRIFT
            double driftPercent = (random.nextDouble() * 2 - 1) * MAX_DRIFT_PERCENT;
            lastPrice = lastPrice * (1 + driftPercent);
            
            // Round to 2 decimal places (cents)
            lastPrice = Math.round(lastPrice * 100.0) / 100.0;
            
            // Ensure price stays positive
            if (lastPrice < 0.01) {
                lastPrice = 0.01;
            }
            
            return lastPrice;
        }
        
        /**
         * Gets the half-spread for bid/ask calculation.
         */
        double getHalfSpread() {
            // Half-spread is a percentage of price, minimum 0.01
            return Math.max(0.01, lastPrice * SPREAD_PERCENT);
        }
    }
}
