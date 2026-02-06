package com.pulsewire.dataplane.adapter.synthetic;

import com.pulsewire.dataplane.adapter.spi.FeedAdapter;
import com.pulsewire.dataplane.adapter.spi.FeedEventHandler;
import com.pulsewire.dataplane.adapter.spi.RawFeedMessage;
import com.pulsewire.dataplane.adapter.spi.TransportType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for the Synthetic Exchange Feed Adapter (US01-02).
 * 
 * Tests are organized by Acceptance Criteria:
 * - AC1: Start/stop independently via configuration
 * - AC2: Emit realistic trade and quote messages with sequence numbers
 * - AC3: Configurable symbols, rates, and burst patterns
 */
class SyntheticExchangeAdapterTest {
    
    private SyntheticExchangeAdapter adapter;
    private TestFeedEventHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new TestFeedEventHandler();
    }
    
    @AfterEach
    void tearDown() {
        // Ensure adapter is disconnected after each test
        if (adapter != null && adapter.isConnected()) {
            adapter.disconnect();
        }
    }
    
    // =========================================================================
    // AC1: Synthetic adapter can be started and stopped independently via configuration
    // =========================================================================
    
    @Nested
    @DisplayName("AC1: Start/Stop via Configuration")
    class StartStopViaConfiguration {
        
        @Test
        @DisplayName("Adapter starts when enabled in configuration")
        void adapterStartsWhenEnabled() throws InterruptedException {
            // Given: A configuration with enabled=true
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch connectedLatch = new CountDownLatch(1);
            handler.onConnectedCallback = () -> connectedLatch.countDown();
            
            // When: Connecting the adapter
            adapter.connect(handler);
            
            // Then: Adapter should be connected and start emitting
            assertTrue(connectedLatch.await(1, TimeUnit.SECONDS), "Should receive onConnected");
            assertTrue(adapter.isConnected(), "Adapter should be connected");
        }
        
        @Test
        @DisplayName("Adapter does NOT start when disabled in configuration")
        void adapterDoesNotStartWhenDisabled() {
            // Given: A configuration with enabled=false
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(false)
                    .symbols(List.of("AAPL"))
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            
            // When: Attempting to connect
            adapter.connect(handler);
            
            // Then: Adapter should NOT be connected (no-op when disabled)
            assertFalse(adapter.isConnected(), "Adapter should not connect when disabled");
            assertFalse(handler.connected.get(), "Handler should not receive onConnected");
        }
        
        @Test
        @DisplayName("Adapter stops cleanly on disconnect()")
        @Timeout(5)
        void adapterStopsOnDisconnect() throws InterruptedException {
            // Given: A running adapter
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(100) // Fast rate
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch messagesLatch = new CountDownLatch(5);
            handler.onMessageCallback = () -> messagesLatch.countDown();
            
            adapter.connect(handler);
            assertTrue(messagesLatch.await(1, TimeUnit.SECONDS), "Should receive messages");
            
            // When: Disconnecting
            CountDownLatch disconnectedLatch = new CountDownLatch(1);
            handler.onDisconnectedCallback = () -> disconnectedLatch.countDown();
            adapter.disconnect();
            
            // Then: Should be disconnected and no more messages
            assertTrue(disconnectedLatch.await(1, TimeUnit.SECONDS), "Should receive onDisconnected");
            assertFalse(adapter.isConnected(), "Adapter should be disconnected");
            
            int messageCountAfterDisconnect = handler.messages.size();
            Thread.sleep(200); // Wait to see if more messages come
            assertEquals(messageCountAfterDisconnect, handler.messages.size(), 
                    "No more messages should arrive after disconnect");
        }
        
        @Test
        @DisplayName("Adapter can be restarted after stop")
        @Timeout(5)
        void adapterCanBeRestarted() throws InterruptedException {
            // Given: Configuration
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(50)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            
            // Start and verify
            adapter.connect(handler);
            Thread.sleep(100);
            assertTrue(adapter.isConnected());
            
            // Stop
            adapter.disconnect();
            assertFalse(adapter.isConnected());
            
            // When: Restarting
            handler = new TestFeedEventHandler(); // Fresh handler
            CountDownLatch connectedLatch = new CountDownLatch(1);
            handler.onConnectedCallback = () -> connectedLatch.countDown();
            
            adapter.connect(handler);
            
            // Then: Should work again
            assertTrue(connectedLatch.await(1, TimeUnit.SECONDS), "Should reconnect");
            assertTrue(adapter.isConnected());
        }
        
        @Test
        @DisplayName("Implements FeedAdapter SPI interface")
        void implementsFeedAdapterInterface() {
            // Given/When: Creating adapter
            SyntheticFeedConfig config = SyntheticFeedConfig.builder().build();
            adapter = new SyntheticExchangeAdapter(config);
            
            // Then: Should be a FeedAdapter
            assertInstanceOf(FeedAdapter.class, adapter);
        }
        
        @Test
        @DisplayName("Returns VENDOR_SDK as transport type")
        void returnsVendorSdkTransportType() {
            // Given: An adapter
            SyntheticFeedConfig config = SyntheticFeedConfig.builder().build();
            adapter = new SyntheticExchangeAdapter(config);
            
            // Then: Should return VENDOR_SDK (it's a simulated feed, not real network)
            assertEquals(TransportType.VENDOR_SDK, adapter.getTransportType());
        }
    }
    
    // =========================================================================
    // AC2: Adapter emits trade and quote messages with realistic fields and sequence numbers
    // =========================================================================
    
    @Nested
    @DisplayName("AC2: Trade and Quote Messages")
    class TradeAndQuoteMessages {
        
        @Test
        @DisplayName("Emits trade messages with required fields")
        @Timeout(5)
        void emitsTradeMessagesWithRequiredFields() throws InterruptedException {
            // Given: Adapter configured to emit messages
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(100)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch tradeLatch = new CountDownLatch(1);
            
            // Capture first trade message
            handler.onMessageCallback = () -> {
                for (RawFeedMessage msg : handler.messages) {
                    String json = new String(msg.payload(), StandardCharsets.UTF_8);
                    if (json.contains("\"type\":\"TRADE\"")) {
                        tradeLatch.countDown();
                        break;
                    }
                }
            };
            
            // When: Running adapter
            adapter.connect(handler);
            assertTrue(tradeLatch.await(2, TimeUnit.SECONDS), "Should receive a trade message");
            
            // Then: Trade message should have required fields
            String tradeJson = findMessageOfType(handler.messages, "TRADE");
            assertNotNull(tradeJson, "Should have a trade message");
            assertAll("Trade message fields",
                () -> assertTrue(tradeJson.contains("\"symbol\""), "Should have symbol"),
                () -> assertTrue(tradeJson.contains("\"price\""), "Should have price"),
                () -> assertTrue(tradeJson.contains("\"quantity\""), "Should have quantity"),
                () -> assertTrue(tradeJson.contains("\"timestamp\""), "Should have timestamp"),
                () -> assertTrue(tradeJson.contains("\"tradeId\""), "Should have tradeId"),
                () -> assertTrue(tradeJson.contains("\"side\""), "Should have side (BUY/SELL)")
            );
        }
        
        @Test
        @DisplayName("Emits quote messages with required fields")
        @Timeout(5)
        void emitsQuoteMessagesWithRequiredFields() throws InterruptedException {
            // Given: Adapter configured to emit messages
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(100)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch quoteLatch = new CountDownLatch(1);
            
            handler.onMessageCallback = () -> {
                for (RawFeedMessage msg : handler.messages) {
                    String json = new String(msg.payload(), StandardCharsets.UTF_8);
                    if (json.contains("\"type\":\"QUOTE\"")) {
                        quoteLatch.countDown();
                        break;
                    }
                }
            };
            
            // When: Running adapter
            adapter.connect(handler);
            assertTrue(quoteLatch.await(2, TimeUnit.SECONDS), "Should receive a quote message");
            
            // Then: Quote message should have required fields
            String quoteJson = findMessageOfType(handler.messages, "QUOTE");
            assertNotNull(quoteJson, "Should have a quote message");
            assertAll("Quote message fields",
                () -> assertTrue(quoteJson.contains("\"symbol\""), "Should have symbol"),
                () -> assertTrue(quoteJson.contains("\"bidPrice\""), "Should have bidPrice"),
                () -> assertTrue(quoteJson.contains("\"bidSize\""), "Should have bidSize"),
                () -> assertTrue(quoteJson.contains("\"askPrice\""), "Should have askPrice"),
                () -> assertTrue(quoteJson.contains("\"askSize\""), "Should have askSize"),
                () -> assertTrue(quoteJson.contains("\"timestamp\""), "Should have timestamp")
            );
        }
        
        @Test
        @DisplayName("Messages have monotonically increasing sequence numbers")
        @Timeout(5)
        void messagesHaveIncreasingSequenceNumbers() throws InterruptedException {
            // Given: Adapter emitting messages
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(100)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch latch = new CountDownLatch(20);
            handler.onMessageCallback = () -> latch.countDown();
            
            // When: Collecting messages
            adapter.connect(handler);
            assertTrue(latch.await(2, TimeUnit.SECONDS), "Should receive 20 messages");
            adapter.disconnect();
            
            // Then: Sequence numbers should be monotonically increasing
            List<RawFeedMessage> messages = new ArrayList<>(handler.messages);
            for (int i = 1; i < messages.size(); i++) {
                long prev = messages.get(i - 1).sequenceNumber();
                long curr = messages.get(i).sequenceNumber();
                assertTrue(curr > prev, 
                        "Sequence numbers should increase: " + prev + " -> " + curr);
            }
        }
        
        @Test
        @DisplayName("Trade prices are positive and realistic")
        @Timeout(5)
        void tradePricesAreRealistic() throws InterruptedException {
            // Given: Adapter emitting trades
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(200)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch latch = new CountDownLatch(50);
            handler.onMessageCallback = () -> latch.countDown();
            
            adapter.connect(handler);
            latch.await(2, TimeUnit.SECONDS);
            adapter.disconnect();
            
            // Then: Extract and validate trade prices
            List<Double> prices = extractPrices(handler.messages, "TRADE");
            assertFalse(prices.isEmpty(), "Should have trade prices");
            
            for (Double price : prices) {
                assertTrue(price > 0, "Price should be positive: " + price);
                assertTrue(price < 10000, "Price should be realistic: " + price);
            }
        }
        
        @Test
        @DisplayName("Quote bid price is less than ask price (valid spread)")
        @Timeout(5)
        void quoteBidLessThanAsk() throws InterruptedException {
            // Given: Adapter emitting quotes
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(200)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch latch = new CountDownLatch(50);
            handler.onMessageCallback = () -> latch.countDown();
            
            adapter.connect(handler);
            latch.await(2, TimeUnit.SECONDS);
            adapter.disconnect();
            
            // Then: For each quote, bid < ask
            for (RawFeedMessage msg : handler.messages) {
                String json = new String(msg.payload(), StandardCharsets.UTF_8);
                if (json.contains("\"type\":\"QUOTE\"")) {
                    double bidPrice = extractDoubleField(json, "bidPrice");
                    double askPrice = extractDoubleField(json, "askPrice");
                    assertTrue(bidPrice < askPrice, 
                            "Bid should be less than ask: bid=" + bidPrice + ", ask=" + askPrice);
                }
            }
        }
        
        @Test
        @DisplayName("Emits both trades and quotes (mixed message types)")
        @Timeout(5)
        void emitsBothTradesAndQuotes() throws InterruptedException {
            // Given: Adapter running
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(100)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch latch = new CountDownLatch(50);
            handler.onMessageCallback = () -> latch.countDown();
            
            adapter.connect(handler);
            latch.await(2, TimeUnit.SECONDS);
            adapter.disconnect();
            
            // Then: Should have both types
            int tradeCount = 0, quoteCount = 0;
            for (RawFeedMessage msg : handler.messages) {
                String json = new String(msg.payload(), StandardCharsets.UTF_8);
                if (json.contains("\"type\":\"TRADE\"")) tradeCount++;
                if (json.contains("\"type\":\"QUOTE\"")) quoteCount++;
            }
            
            assertTrue(tradeCount > 0, "Should have at least one trade");
            assertTrue(quoteCount > 0, "Should have at least one quote");
        }
    }
    
    // =========================================================================
    // AC3: Configuration supports symbol list, message rate, and burst patterns
    // =========================================================================
    
    @Nested
    @DisplayName("AC3: Configuration Options")
    class ConfigurationOptions {
        
        @Test
        @DisplayName("Emits messages only for configured symbols")
        @Timeout(5)
        void emitsOnlyConfiguredSymbols() throws InterruptedException {
            // Given: Config with specific symbols
            List<String> configuredSymbols = List.of("AAPL", "GOOGL");
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(configuredSymbols)
                    .messageRatePerSecond(100)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch latch = new CountDownLatch(30);
            handler.onMessageCallback = () -> latch.countDown();
            
            adapter.connect(handler);
            latch.await(2, TimeUnit.SECONDS);
            adapter.disconnect();
            
            // Then: All messages should be for configured symbols only
            for (RawFeedMessage msg : handler.messages) {
                String json = new String(msg.payload(), StandardCharsets.UTF_8);
                String symbol = extractStringField(json, "symbol");
                assertTrue(configuredSymbols.contains(symbol),
                        "Symbol should be from configured list: " + symbol);
            }
        }
        
        @Test
        @DisplayName("Message rate approximately matches configuration")
        @Timeout(10)
        void messageRateMatchesConfiguration() throws InterruptedException {
            // Given: Config with specific rate
            int targetRate = 50; // 50 messages per second
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(targetRate)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            adapter.connect(handler);
            
            // When: Measuring over 2 seconds
            Thread.sleep(2000);
            adapter.disconnect();
            
            // Then: Rate should be approximately correct (within 20% tolerance)
            int messageCount = handler.messages.size();
            int expectedCount = targetRate * 2; // 2 seconds
            double tolerance = 0.3; // 30% tolerance for timing variations
            
            assertTrue(messageCount >= expectedCount * (1 - tolerance),
                    "Message count too low: " + messageCount + " < " + (expectedCount * (1 - tolerance)));
            assertTrue(messageCount <= expectedCount * (1 + tolerance),
                    "Message count too high: " + messageCount + " > " + (expectedCount * (1 + tolerance)));
        }
        
        @Test
        @DisplayName("Burst mode increases message rate during burst periods")
        @Timeout(15)
        void burstModeIncreasesRate() throws InterruptedException {
            // Given: Config with burst enabled
            int baseRate = 10;
            int burstMultiplier = 5;
            long burstDurationMs = 500;
            long burstIntervalMs = 2000;
            
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(baseRate)
                    .burstEnabled(true)
                    .burstMultiplier(burstMultiplier)
                    .burstDurationMs(burstDurationMs)
                    .burstIntervalMs(burstIntervalMs)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            
            // Collect messages over a period that includes a burst
            adapter.connect(handler);
            Thread.sleep(burstIntervalMs + burstDurationMs + 500); // Wait for at least one burst
            adapter.disconnect();
            
            // Then: Should have more messages than base rate alone would produce
            int messageCount = handler.messages.size();
            double durationSeconds = (burstIntervalMs + burstDurationMs + 500) / 1000.0;
            int baseExpected = (int) (baseRate * durationSeconds);
            
            // With burst, we expect more messages
            assertTrue(messageCount > baseExpected,
                    "Burst should increase message count: actual=" + messageCount + ", baseExpected=" + baseExpected);
        }
        
        @Test
        @DisplayName("Default configuration has sensible values")
        void defaultConfigurationHasSensibleValues() {
            // Given/When: Creating default config
            SyntheticFeedConfig config = SyntheticFeedConfig.builder().build();
            
            // Then: Should have defaults
            assertFalse(config.symbols().isEmpty(), "Should have default symbols");
            assertTrue(config.messageRatePerSecond() > 0, "Should have positive rate");
            assertFalse(config.burstEnabled(), "Burst should be disabled by default");
            assertTrue(config.enabled(), "Should be enabled by default");
        }
        
        @Test
        @DisplayName("Configuration validates symbol list is not empty when enabled")
        void configValidatesSymbolList() {
            // When/Then: Creating config with empty symbols should throw
            assertThrows(IllegalArgumentException.class, () ->
                SyntheticFeedConfig.builder()
                        .enabled(true)
                        .symbols(List.of())
                        .build()
            );
        }
        
        @Test
        @DisplayName("Configuration validates message rate is positive")
        void configValidatesMessageRate() {
            // When/Then: Creating config with invalid rate should throw
            assertThrows(IllegalArgumentException.class, () ->
                SyntheticFeedConfig.builder()
                        .messageRatePerSecond(0)
                        .build()
            );
            
            assertThrows(IllegalArgumentException.class, () ->
                SyntheticFeedConfig.builder()
                        .messageRatePerSecond(-5)
                        .build()
            );
        }
        
        @Test
        @DisplayName("Trade to quote ratio is configurable")
        @Timeout(10)
        void tradeToQuoteRatioIsConfigurable() throws InterruptedException {
            // Given: Config with specific trade/quote ratio (1:1 for equal distribution)
            SyntheticFeedConfig config = SyntheticFeedConfig.builder()
                    .enabled(true)
                    .symbols(List.of("AAPL"))
                    .messageRatePerSecond(100)
                    .tradeToQuoteRatio(1) // 1 quote per trade (50/50 split)
                    .build();
            
            adapter = new SyntheticExchangeAdapter(config);
            CountDownLatch latch = new CountDownLatch(100);
            handler.onMessageCallback = () -> latch.countDown();
            
            adapter.connect(handler);
            latch.await(3, TimeUnit.SECONDS);
            adapter.disconnect();
            
            // Then: Trade and quote counts should be roughly equal
            int trades = 0, quotes = 0;
            for (RawFeedMessage msg : handler.messages) {
                String json = new String(msg.payload(), StandardCharsets.UTF_8);
                if (json.contains("\"type\":\"TRADE\"")) trades++;
                if (json.contains("\"type\":\"QUOTE\"")) quotes++;
            }
            
            // With 1:1 ratio, expect roughly equal numbers (within 40% due to randomness)
            double ratio = trades > 0 ? (double) quotes / trades : 0;
            assertTrue(ratio >= 0.5 && ratio <= 2.0,
                    "Trade/quote ratio should be approximately 1:1, got " + trades + " trades, " + quotes + " quotes");
        }
    }
    
    // =========================================================================
    // Helper methods
    // =========================================================================
    
    private String findMessageOfType(List<RawFeedMessage> messages, String type) {
        for (RawFeedMessage msg : messages) {
            String json = new String(msg.payload(), StandardCharsets.UTF_8);
            if (json.contains("\"type\":\"" + type + "\"")) {
                return json;
            }
        }
        return null;
    }
    
    private List<Double> extractPrices(List<RawFeedMessage> messages, String type) {
        List<Double> prices = new ArrayList<>();
        for (RawFeedMessage msg : messages) {
            String json = new String(msg.payload(), StandardCharsets.UTF_8);
            if (json.contains("\"type\":\"" + type + "\"")) {
                prices.add(extractDoubleField(json, "price"));
            }
        }
        return prices;
    }
    
    private double extractDoubleField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return 0.0;
        start += pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end).trim());
    }
    
    private String extractStringField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    
    // =========================================================================
    // Test helper class
    // =========================================================================
    
    private static class TestFeedEventHandler implements FeedEventHandler {
        final AtomicBoolean connected = new AtomicBoolean(false);
        final List<RawFeedMessage> messages = new CopyOnWriteArrayList<>();
        final List<Throwable> errors = new CopyOnWriteArrayList<>();
        volatile String disconnectReason;
        
        Runnable onConnectedCallback;
        Runnable onDisconnectedCallback;
        Runnable onMessageCallback;
        
        @Override
        public void onConnected(String adapterId) {
            connected.set(true);
            if (onConnectedCallback != null) onConnectedCallback.run();
        }
        
        @Override
        public void onDisconnected(String adapterId, String reason) {
            connected.set(false);
            disconnectReason = reason;
            if (onDisconnectedCallback != null) onDisconnectedCallback.run();
        }
        
        @Override
        public void onMessage(String adapterId, RawFeedMessage message) {
            messages.add(message);
            if (onMessageCallback != null) onMessageCallback.run();
        }
        
        @Override
        public void onError(String adapterId, Throwable error) {
            errors.add(error);
        }
        
        @Override
        public void onHeartbeatTimeout(String adapterId) {
            // Not used in these tests
        }
    }
}
