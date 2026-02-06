package com.pulsewire.dataplane.adapter.synthetic;

import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration for the Synthetic Exchange Feed Adapter.
 * 
 * <p>This configuration controls all aspects of synthetic data generation:
 * <ul>
 *   <li><b>enabled</b>: Whether the adapter should start when connected</li>
 *   <li><b>symbols</b>: List of instruments to generate data for</li>
 *   <li><b>messageRatePerSecond</b>: Base rate of message generation</li>
 *   <li><b>burst*</b>: Configuration for simulating market burst events</li>
 *   <li><b>tradeToQuoteRatio</b>: Ratio of quotes per trade (markets have more quotes)</li>
 * </ul>
 * 
 * <h2>Design Decision (ADR-004)</h2>
 * <p>Configuration is immutable to ensure thread-safe sharing between the adapter
 * and any management components. Changes require creating a new configuration
 * and reconnecting the adapter.
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * SyntheticFeedConfig config = SyntheticFeedConfig.builder()
 *     .enabled(true)
 *     .symbols(List.of("AAPL", "GOOGL", "MSFT"))
 *     .messageRatePerSecond(50)
 *     .burstEnabled(true)
 *     .burstMultiplier(5)
 *     .build();
 * }</pre>
 * 
 * @param enabled Whether the adapter is enabled (false = connect() is a no-op)
 * @param symbols List of instrument symbols to generate data for
 * @param messageRatePerSecond Base rate of message generation
 * @param burstEnabled Whether burst mode is active
 * @param burstMultiplier Rate multiplier during burst periods
 * @param burstDurationMs Duration of each burst in milliseconds
 * @param burstIntervalMs Time between burst starts in milliseconds
 * @param tradeToQuoteRatio Number of quotes generated per trade (default: 5)
 */
public record SyntheticFeedConfig(
        boolean enabled,
        List<String> symbols,
        int messageRatePerSecond,
        boolean burstEnabled,
        int burstMultiplier,
        long burstDurationMs,
        long burstIntervalMs,
        int tradeToQuoteRatio
) {
    
    // Default values for configuration
    private static final List<String> DEFAULT_SYMBOLS = List.of("AAPL", "GOOGL", "MSFT");
    private static final int DEFAULT_RATE = 10;
    private static final int DEFAULT_BURST_MULTIPLIER = 5;
    private static final long DEFAULT_BURST_DURATION_MS = 1000;
    private static final long DEFAULT_BURST_INTERVAL_MS = 10000;
    private static final int DEFAULT_TRADE_TO_QUOTE_RATIO = 5;
    
    /**
     * Compact constructor for validation.
     */
    public SyntheticFeedConfig {
        // Validate symbols when enabled
        if (enabled && (symbols == null || symbols.isEmpty())) {
            throw new IllegalArgumentException("Symbols cannot be empty when adapter is enabled");
        }
        
        // Ensure symbols is immutable
        symbols = symbols == null ? List.of() : List.copyOf(symbols);
        
        // Validate rate
        if (messageRatePerSecond <= 0) {
            throw new IllegalArgumentException("Message rate must be positive: " + messageRatePerSecond);
        }
        
        // Validate burst settings if enabled
        if (burstEnabled) {
            if (burstMultiplier <= 1) {
                throw new IllegalArgumentException("Burst multiplier must be > 1: " + burstMultiplier);
            }
            if (burstDurationMs <= 0) {
                throw new IllegalArgumentException("Burst duration must be positive: " + burstDurationMs);
            }
            if (burstIntervalMs <= burstDurationMs) {
                throw new IllegalArgumentException(
                    "Burst interval must be > duration: interval=" + burstIntervalMs + ", duration=" + burstDurationMs
                );
            }
        }
        
        // Validate trade/quote ratio
        if (tradeToQuoteRatio < 1) {
            throw new IllegalArgumentException("Trade to quote ratio must be >= 1: " + tradeToQuoteRatio);
        }
    }
    
    /**
     * Creates a new builder for constructing configuration.
     * 
     * @return A new builder with default values
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for {@link SyntheticFeedConfig} with sensible defaults.
     * 
     * <p><b>Design Decision (ADR-005):</b> Builder pattern provides a clean API
     * for configuration with many optional parameters and validation at build time.
     */
    public static class Builder {
        private boolean enabled = true; // Enabled by default
        private List<String> symbols = DEFAULT_SYMBOLS;
        private int messageRatePerSecond = DEFAULT_RATE;
        private boolean burstEnabled = false;
        private int burstMultiplier = DEFAULT_BURST_MULTIPLIER;
        private long burstDurationMs = DEFAULT_BURST_DURATION_MS;
        private long burstIntervalMs = DEFAULT_BURST_INTERVAL_MS;
        private int tradeToQuoteRatio = DEFAULT_TRADE_TO_QUOTE_RATIO;
        
        /**
         * Sets whether the adapter is enabled.
         * 
         * @param enabled true to enable, false to disable (connect becomes no-op)
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        /**
         * Sets the list of symbols to generate data for.
         * 
         * @param symbols list of instrument symbols
         * @return this builder
         */
        public Builder symbols(List<String> symbols) {
            this.symbols = symbols;
            return this;
        }
        
        /**
         * Sets the base message rate per second.
         * 
         * @param rate messages per second (must be positive)
         * @return this builder
         */
        public Builder messageRatePerSecond(int rate) {
            this.messageRatePerSecond = rate;
            return this;
        }
        
        /**
         * Enables or disables burst mode.
         * 
         * @param enabled true to enable burst mode
         * @return this builder
         */
        public Builder burstEnabled(boolean enabled) {
            this.burstEnabled = enabled;
            return this;
        }
        
        /**
         * Sets the rate multiplier during burst periods.
         * 
         * @param multiplier rate multiplier (must be > 1)
         * @return this builder
         */
        public Builder burstMultiplier(int multiplier) {
            this.burstMultiplier = multiplier;
            return this;
        }
        
        /**
         * Sets the duration of each burst period.
         * 
         * @param durationMs burst duration in milliseconds
         * @return this builder
         */
        public Builder burstDurationMs(long durationMs) {
            this.burstDurationMs = durationMs;
            return this;
        }
        
        /**
         * Sets the interval between burst starts.
         * 
         * @param intervalMs interval in milliseconds (must be > burstDurationMs)
         * @return this builder
         */
        public Builder burstIntervalMs(long intervalMs) {
            this.burstIntervalMs = intervalMs;
            return this;
        }
        
        /**
         * Sets the ratio of quotes to trades.
         * 
         * <p>For example, a ratio of 5 means 5 quotes are generated for every 1 trade.
         * This reflects real market behavior where quote updates far exceed trades.
         * 
         * @param ratio quotes per trade (must be >= 1)
         * @return this builder
         */
        public Builder tradeToQuoteRatio(int ratio) {
            this.tradeToQuoteRatio = ratio;
            return this;
        }
        
        /**
         * Builds the immutable configuration.
         * 
         * @return the configuration
         * @throws IllegalArgumentException if validation fails
         */
        public SyntheticFeedConfig build() {
            return new SyntheticFeedConfig(
                enabled,
                symbols,
                messageRatePerSecond,
                burstEnabled,
                burstMultiplier,
                burstDurationMs,
                burstIntervalMs,
                tradeToQuoteRatio
            );
        }
    }
}
