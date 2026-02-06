package com.pulsewire.dataplane.adapter.synthetic;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Immutable record representing a synthetic quote (top of book) message.
 * 
 * <p>Quotes represent the current best bid and offer prices:
 * <ul>
 *   <li><b>bidPrice/bidSize</b>: Best price/quantity buyers are willing to pay</li>
 *   <li><b>askPrice/askSize</b>: Best price/quantity sellers are offering</li>
 *   <li><b>spread</b>: Difference between ask and bid (askPrice - bidPrice)</li>
 * </ul>
 * 
 * <h2>Market Microstructure</h2>
 * <p>The bid-ask spread represents the cost of immediacy:
 * <ul>
 *   <li>Tight spread (1-5 cents) = liquid stock (AAPL, MSFT)</li>
 *   <li>Wide spread (10+ cents) = illiquid or volatile stock</li>
 * </ul>
 * 
 * @param symbol The instrument identifier
 * @param bidPrice Best bid price (highest price a buyer will pay)
 * @param bidSize Quantity available at the bid
 * @param askPrice Best ask price (lowest price a seller will accept)
 * @param askSize Quantity available at the ask
 * @param timestamp When this quote was generated
 */
public record SyntheticQuote(
        String symbol,
        double bidPrice,
        long bidSize,
        double askPrice,
        long askSize,
        Instant timestamp
) {
    
    /**
     * Compact constructor for validation.
     * 
     * <p>Enforces market structure invariants:
     * <ul>
     *   <li>All prices must be positive</li>
     *   <li>Bid must be less than ask (no crossed market)</li>
     *   <li>Sizes must be positive</li>
     * </ul>
     */
    public SyntheticQuote {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
        if (bidPrice <= 0) {
            throw new IllegalArgumentException("Bid price must be positive: " + bidPrice);
        }
        if (askPrice <= 0) {
            throw new IllegalArgumentException("Ask price must be positive: " + askPrice);
        }
        if (bidPrice >= askPrice) {
            throw new IllegalArgumentException(
                "Bid must be less than ask (no crossed market): bid=" + bidPrice + ", ask=" + askPrice
            );
        }
        if (bidSize <= 0) {
            throw new IllegalArgumentException("Bid size must be positive: " + bidSize);
        }
        if (askSize <= 0) {
            throw new IllegalArgumentException("Ask size must be positive: " + askSize);
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }
    
    /**
     * Calculates the bid-ask spread.
     * 
     * @return The spread in price units
     */
    public double spread() {
        return askPrice - bidPrice;
    }
    
    /**
     * Calculates the mid-price (average of bid and ask).
     * 
     * @return The mid-price
     */
    public double midPrice() {
        return (bidPrice + askPrice) / 2.0;
    }
    
    /**
     * Serializes this quote to JSON format.
     * 
     * <p>Example output:
     * <pre>{@code
     * {"type":"QUOTE","symbol":"AAPL","bidPrice":185.48,"bidSize":100,"askPrice":185.52,"askSize":200,"timestamp":"2026-02-06T10:30:00Z"}
     * }</pre>
     * 
     * @return JSON string representation
     */
    public String toJson() {
        return String.format(
            "{\"type\":\"QUOTE\",\"symbol\":\"%s\",\"bidPrice\":%.4f,\"bidSize\":%d,\"askPrice\":%.4f,\"askSize\":%d,\"timestamp\":\"%s\"}",
            symbol,
            bidPrice,
            bidSize,
            askPrice,
            askSize,
            timestamp.toString()
        );
    }
    
    /**
     * Converts this quote to bytes for wire transmission.
     * 
     * @return UTF-8 encoded bytes of the JSON representation
     */
    public byte[] toBytes() {
        return toJson().getBytes(StandardCharsets.UTF_8);
    }
}
