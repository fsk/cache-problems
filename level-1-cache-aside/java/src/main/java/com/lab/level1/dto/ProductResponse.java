package com.lab.level1.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        int popularityScore,
        Instant updatedAt,
        String servedFrom
) {
    public ProductResponse withServedFrom(String source) {
        return new ProductResponse(id, name, sku, price, popularityScore, updatedAt, source);
    }
}
