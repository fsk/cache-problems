package com.lab.distributed.cache;

import com.lab.distributed.config.LabProperties;
import com.lab.distributed.logging.LabLog;
import com.lab.distributed.logging.LabLogger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLabCache implements LabCache {

    private final StringRedisTemplate redis;
    private final LabProperties properties;
    private final LabLogger log = LabLogger.of(RedisLabCache.class);

    public RedisLabCache(StringRedisTemplate redis, LabProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    @Override
    public Optional<String> get(String key) {
        String resolved = resolveKey(key);
        String value = redis.opsForValue().get(resolved);
        if (value != null) {
            log.debug(LabLog.CACHE_HIT, "redis get", "key", resolved);
            return Optional.of(value);
        }
        log.info(LabLog.CACHE_MISS, "redis get", "key", resolved);
        return Optional.empty();
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        String resolved = resolveKey(key);
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            redis.opsForValue().set(resolved, value);
        } else {
            redis.opsForValue().set(resolved, value, ttl.toSeconds(), TimeUnit.SECONDS);
        }
        log.info(LabLog.CACHE_PUT, "redis set", "key", resolved, "ttlSeconds", ttl != null ? ttl.toSeconds() : -1);
    }

    @Override
    public void delete(String key) {
        String resolved = resolveKey(key);
        redis.delete(resolved);
        log.info(LabLog.CACHE_EVICT, "redis delete", "key", resolved);
    }

    @Override
    public boolean exists(String key) {
        Boolean exists = redis.hasKey(resolveKey(key));
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public String resolveKey(String rawKey) {
        String prefix = properties.cache().keyPrefix();
        if (prefix == null || prefix.isBlank()) {
            return rawKey;
        }
        return prefix.endsWith(":") ? prefix + rawKey : prefix + ":" + rawKey;
    }
}
