package com.lab.level1.logging;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * Level-1 only — wraps DB access to emit [DB QUERY EXECUTED] with timing.
 */
@Slf4j
public final class DbQueryLogger {


    private DbQueryLogger() {}

    public static <T> T execute(String operation, Supplier<T> query) {
        long start = System.nanoTime();
        T result = query.get();
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        log.info("{} operation={} durationMs={}", Level1Log.DB_QUERY, operation, durationMs);
        return result;
    }
}
