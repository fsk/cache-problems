package com.lab.distributed.cache;

import com.lab.distributed.logging.LabLog;
import com.lab.distributed.logging.LabLogger;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Reference cache-aside flow WITHOUT stampede protection or invalidation.
 * Levels may use this or roll their own broken variant.
 */
public class CacheAsideTemplate {

    private final LabCache cache;
    private final LabLogger log = LabLogger.of(CacheAsideTemplate.class);

    public CacheAsideTemplate(LabCache cache) {
        this.cache = cache;
    }

    /**
     * Classic cache-aside: read cache → on miss load from DB → write cache.
     */
    public String getOrLoad(String cacheKey, Duration ttl, Supplier<Optional<String>> dbLoader) {
        Optional<String> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            log.info(LabLog.CACHE_HIT, "served from cache", "key", cacheKey);
            return cached.get();
        }

        log.info(LabLog.CACHE_MISS, "loading from db", "key", cacheKey);
        Optional<String> fromDb = dbLoader.get();
        if (fromDb.isEmpty()) {
            return null;
        }
        cache.put(cacheKey, fromDb.get(), ttl);
        return fromDb.get();
    }
}
