package com.pulsewire.core.model;

import java.math.BigDecimal;

/**
 * Quote event payload (top-of-book bid/ask).
 */
public record Quote(
        BigDecimal bidPrice,
        BigDecimal bidSize,
        BigDecimal askPrice,
        BigDecimal askSize) {
}
