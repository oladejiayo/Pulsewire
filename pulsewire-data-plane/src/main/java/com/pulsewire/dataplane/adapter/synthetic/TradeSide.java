package com.pulsewire.dataplane.adapter.synthetic;

/**
 * Represents the aggressor side of a trade.
 * 
 * <p>In market data, trades report which side (buyer or seller)
 * initiated the transaction by crossing the spread.
 * 
 * <ul>
 *   <li>{@link #BUY} - Buyer crossed the spread (hit the ask)</li>
 *   <li>{@link #SELL} - Seller crossed the spread (hit the bid)</li>
 * </ul>
 */
public enum TradeSide {
    /** Buyer initiated the trade by hitting the ask price */
    BUY,
    
    /** Seller initiated the trade by hitting the bid price */
    SELL
}
