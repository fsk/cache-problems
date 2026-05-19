package com.lab.distributed.util;

import com.lab.distributed.logging.LabLog;
import com.lab.distributed.logging.LabLogger;

import java.util.function.Supplier;

/**
 * Wraps repository/DAO calls to emit [DB QUERY EXECUTED] with timing.
 */
public final class DbQueryLogger {

    private static final LabLogger LOG = LabLogger.of(DbQueryLogger.class);

    private DbQueryLogger() {}

    public static <T> T logQuery(String operation, Supplier<T> query) {
        long start = System.nanoTime();
        try {
            T result = query.get();
            long ms = (System.nanoTime() - start) / 1_000_000;
            LOG.info(LabLog.DB_QUERY, operation, "durationMs", ms);
            return result;
        } catch (RuntimeException ex) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            LOG.error(LabLog.DB_QUERY, operation + " failed", "durationMs", ms, "error", ex.getMessage());
            throw ex;
        }
    }
}
