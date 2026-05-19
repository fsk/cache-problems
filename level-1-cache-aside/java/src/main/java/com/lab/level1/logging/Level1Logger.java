package com.lab.level1.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class Level1Logger {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String RED = "\u001B[31m";

    private final Logger delegate;

    public Level1Logger() {
        this.delegate = LoggerFactory.getLogger(Level1Logger.class);
    }

    public void cacheHit(String cacheName, Object key) {
        log(Level1Log.CACHE_HIT, GREEN, "cache=" + cacheName, "key", key);
    }

    public void cacheMiss(String cacheName, Object key) {
        log(Level1Log.CACHE_MISS, YELLOW, "cache=" + cacheName, "key", key);
    }

    public void cachePut(String cacheName, Object key) {
        log(Level1Log.CACHE_PUT, BLUE, "cache=" + cacheName, "key", key);
    }

    public void cacheEvict(String cacheName, Object key) {
        log(Level1Log.CACHE_EVICT, RED, "cache=" + cacheName, "key", key);
    }

    public void dbQuery(String operation, long durationMs) {
        log(Level1Log.DB_QUERY, MAGENTA, "operation=" + operation, "durationMs", durationMs);
    }

    private void log(String tag, String color, String message, Object... kv) {
        delegate.info("{}{}{} {} {}", color, tag, RESET, message, format(kv));
    }

    private static String format(Object... kv) {
        if (kv == null || kv.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(kv[i]).append('=').append(i + 1 < kv.length ? kv[i + 1] : "?");
        }
        return sb.toString();
    }
}
