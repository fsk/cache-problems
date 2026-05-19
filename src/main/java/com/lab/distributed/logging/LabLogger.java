package com.lab.distributed.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structured logging helper — keeps tag + key=value consistent across levels.
 */
public final class LabLogger {

    private final Logger delegate;

    private LabLogger(Class<?> type) {
        this.delegate = LoggerFactory.getLogger(type);
    }

    public static LabLogger of(Class<?> type) {
        return new LabLogger(type);
    }

    public void info(String tag, String message, Object... keyValues) {
        delegate.info("{} {} {}", tag, message, formatKeyValues(keyValues));
    }

    public void warn(String tag, String message, Object... keyValues) {
        delegate.warn("{} {} {}", tag, message, formatKeyValues(keyValues));
    }

    public void error(String tag, String message, Object... keyValues) {
        delegate.error("{} {} {}", tag, message, formatKeyValues(keyValues));
    }

    public void debug(String tag, String message, Object... keyValues) {
        delegate.debug("{} {} {}", tag, message, formatKeyValues(keyValues));
    }

    private static String formatKeyValues(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) {
                sb.append(' ');
            }
            String key = String.valueOf(keyValues[i]);
            Object value = i + 1 < keyValues.length ? keyValues[i + 1] : "?";
            sb.append(key).append('=').append(value);
        }
        return sb.toString();
    }
}
