package com.lab.level1.logging;

/** Tracks whether the current request was served from cache (set by {@link com.lab.level1.cache.LoggingCache}). */
public final class CacheAccessContext {

    private static final ThreadLocal<Boolean> CACHE_HIT = new ThreadLocal<>();

    private CacheAccessContext() {}

    public static void clear() {
        CACHE_HIT.remove();
    }

    public static void markHit() {
        CACHE_HIT.set(true);
    }

    public static void markMiss() {
        CACHE_HIT.set(false);
    }

    public static boolean wasCacheHit() {
        return Boolean.TRUE.equals(CACHE_HIT.get());
    }
}
