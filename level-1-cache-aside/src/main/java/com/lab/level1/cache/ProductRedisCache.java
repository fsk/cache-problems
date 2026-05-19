package com.lab.level1.cache;

import com.lab.level1.config.Level1Properties;
import com.lab.level1.dto.CachedProduct;
import com.lab.level1.logging.Level1Log;
import com.lab.level1.logging.Level1Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Cache access via {@link RedisTemplate} + Jackson2JsonRedisSerializer (Lettuce driver underneath).
 */
@Component
@RequiredArgsConstructor
public class ProductRedisCache {

    private final RedisTemplate<String, CachedProduct> redis;
    private final Level1Properties properties;
    private final Level1Logger logger;


    public Optional<CachedProduct> get(long productId) {
        String key = cacheKey(productId);
        CachedProduct value = redis.opsForValue().get(key);
        if (value != null) {
            logger.info(Level1Log.CACHE_HIT, "redis get", "key", key);
            return Optional.of(value);
        }
        logger.info(Level1Log.CACHE_MISS, "redis get", "key", key);
        return Optional.empty();
    }

    public void put(long productId, CachedProduct product) {
        String key = cacheKey(productId);
        int ttl = properties.cache().ttlSeconds();
        redis.opsForValue().set(key, product, ttl, TimeUnit.SECONDS);
        logger.info(Level1Log.CACHE_PUT, "redis set", "key", key, "ttlSeconds", ttl);
    }

    public void evict(long productId) {
        String key = cacheKey(productId);
        redis.delete(key);
        logger.info(Level1Log.CACHE_EVICT, "redis delete", "key", key);
    }

    private String cacheKey(long productId) {
        return properties.cache().keyPrefix() + ":" + productId;
    }
}
