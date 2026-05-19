package com.lab.level1.logging;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Level-1 only — wraps DB access to emit colored [DB QUERY EXECUTED] with timing.
 */
@Component
public final class DbQueryLogger {

    private final Level1Logger logger;

    public DbQueryLogger(Level1Logger logger) {
        this.logger = logger;
    }

    public <T> T execute(String operation, Supplier<T> query) {
        long start = System.nanoTime();
        T result = query.get();
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        logger.dbQuery(operation, durationMs);
        return result;
    }
}
