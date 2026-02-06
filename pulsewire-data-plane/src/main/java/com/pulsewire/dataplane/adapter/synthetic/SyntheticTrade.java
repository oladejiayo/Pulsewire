package com.pulsewire.dataplane.adapter.synthetic;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Immutable record representing a synthetic trade message.
 * 
 * <p>Trades represent executed transactions with the following key fields:
 * <ul>
 *   <li><b>symbol</b>: The instrument identifier (e.g., "AAPL")</li>
 *   <li><b>price</b>: The execution price</li>
 *   <li><b>quantity</b>: Number of shares/contracts traded</li>
 *   <li><b>side</b>: Which side initiated (BUY = hit ask, SELL = hit bid)</li>
 *   <li><b>tradeId</b>: Unique identifier for this trade</li>
 *   <li><b>timestamp</b>: When the trade occurred</li>
 * </ul>
 * 
 * <h2>JSON Serialization</h2>
 * <p>Provides {@link #toJson()} for human-readable serialization and
 * {@link #toBytes()} for wire format. Uses simple string formatting
 * to avoid external JSON library dependencies in the SPI layer.
 * 
 * @param symbol The instrument identifier
 * @param price The execution price (must be positive)
 * @param quantity The number of shares/contracts (must be positive)
 * @param timestamp When the trade occurred
 * @param tradeId Unique trade identifier
 * @param side The aggressor side (BUY or SELL)
 */
public record SyntheticTrade(
        String symbol,
        double price,
        long quantity,
        Instant timestamp,
        String tradeId,
        TradeSide side
) {
    
    /**
     * Compact constructor for validation.
     */
    public SyntheticTrade {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + price);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (tradeId == null || tradeId.isBlank()) {
            throw new IllegalArgumentException("Trade ID cannot be null or blank");
        }
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }
    }
    
    /**
     * Serializes this trade to JSON format.
     * 
     * <p>Example output:
     * <pre>{@code
     * {"type":"TRADE","symbol":"AAPL","price":185.50,"quantity":100,"timestamp":"2026-02-06T10:30:00Z","tradeId":"T123","side":"BUY"}
     * }</pre>
     * 
     * @return JSON string representation
     */
    public String toJson() {
        // Using String.format for simple, dependency-free JSON
        // Performance note: In production, consider a pooled StringBuilder or JSON library
        return String.format(
            "{\"type\":\"TRADE\",\"symbol\":\"%s\",\"price\":%.4f,\"quantity\":%d,\"timestamp\":\"%s\",\"tradeId\":\"%s\",\"side\":\"%s\"}",
            symbol,
            price,
            quantity,
            timestamp.toString(),
            tradeId,
            side.name()
        );
    }
    
    /**
     * Converts this trade to bytes for wire transmission.
     * 
     * @return UTF-8 encoded bytes of the JSON representation
     */
    public byte[] toBytes() {
        return toJson().getBytes(StandardCharsets.UTF_8);
    }
}
