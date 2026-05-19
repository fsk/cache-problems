package com.lab.level1.cache;

import com.lab.level1.logging.Level1Logger;
import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LoggingCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final Level1Logger logger;
    private final ConcurrentMap<String, Cache> wrapped = new ConcurrentHashMap<>();

    public LoggingCacheManager(CacheManager delegate, Level1Logger logger) {
        this.delegate = delegate;
        this.logger = logger;
    }

    @Override
    public Cache getCache(@NonNull String name) {
        Cache cache = delegate.getCache(name);
        if (cache == null) {
            return null;
        }
        return wrapped.computeIfAbsent(name, n -> new LoggingCache(cache, logger, n));
    }

    @Override @NonNull
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
