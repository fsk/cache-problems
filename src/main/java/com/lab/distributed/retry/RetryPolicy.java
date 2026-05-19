package com.lab.distributed.retry;

/**
 * Retry policy contract — levels supply broken or naive implementations.
 * Phase 1: no production-grade exponential backoff + jitter in base module.
 */
public record RetryPolicy(
        int maxAttempts,
        long delayMillis,
        boolean retryOnAllExceptions
) {
    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, 0, false);
    }

    public static RetryPolicy naive(int maxAttempts, long delayMillis) {
        return new RetryPolicy(maxAttempts, delayMillis, true);
    }
}
