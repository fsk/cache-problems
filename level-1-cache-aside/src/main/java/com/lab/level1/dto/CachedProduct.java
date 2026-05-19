package com.lab.level1.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Redis value payload — serialized by {@link org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer}.
 */
public record CachedProduct(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        int popularityScore,
        Instant updatedAt
) {}
