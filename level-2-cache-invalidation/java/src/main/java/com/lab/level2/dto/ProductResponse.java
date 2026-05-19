package com.lab.level2.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        Instant updatedAt,
        String source
) implements Serializable {

    public ProductResponse withSource(String source) {
        return new ProductResponse(id, name, sku, price, updatedAt, source);
    }
}
