package com.pulsewire.core.model;

import java.math.BigDecimal;

/**
 * Trade event payload.
 */
public record Trade(
        BigDecimal price,
        BigDecimal size,
        String conditions) {
}
