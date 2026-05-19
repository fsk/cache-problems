package com.lab.level1.cache;

import com.lab.level1.logging.CacheAccessContext;
import com.lab.level1.logging.Level1Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Decorates a Spring {@link Cache} to emit colored [CACHE HIT/MISS/PUT/EVICT] logs for @Cacheable flows.
 */
@RequiredArgsConstructor
public final class LoggingCache implements Cache {

    private final Cache delegate;
    private final Level1Logger logger;
    private final String cacheName;

    @Override @NonNull
    public String getName() {
        return delegate.getName();
    }

    @Override @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        ValueWrapper wrapper = delegate.get(key);
        if (wrapper != null) {
            CacheAccessContext.markHit();
            logger.cacheHit(cacheName, key);
        } else {
            CacheAccessContext.markMiss();
            logger.cacheMiss(cacheName, key);
        }
        return wrapper;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        T value = delegate.get(key, type);
        if (value != null) {
            CacheAccessContext.markHit();
            logger.cacheHit(cacheName, key);
        } else {
            CacheAccessContext.markMiss();
            logger.cacheMiss(cacheName, key);
        }
        return value;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        delegate.put(key, value);
        logger.cachePut(cacheName, key);
    }

    @Override
    public void evict(@NonNull Object key) {
        delegate.evict(key);
        logger.cacheEvict(cacheName, key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
