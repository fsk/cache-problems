package com.lab.distributed.retry;

import com.lab.distributed.logging.LabLog;
import com.lab.distributed.logging.LabLogger;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Naive retry — fixed delay, no jitter, no idempotency awareness.
 * Suitable for level-7 to demonstrate duplicate processing when retries fire.
 */
@Component
public class SimpleRetryExecutor implements RetryExecutor {

    private final LabLogger log = LabLogger.of(SimpleRetryExecutor.class);

    @Override
    public <T> T execute(RetryPolicy policy, Supplier<T> action) {
        int attempts = 0;
        RuntimeException last = null;
        while (attempts < policy.maxAttempts()) {
            attempts++;
            try {
                return action.get();
            } catch (RuntimeException ex) {
                last = ex;
                if (attempts >= policy.maxAttempts()) {
                    break;
                }
                log.warn(LabLog.RETRY_ATTEMPT, "retrying after failure",
                        "attempt", attempts, "maxAttempts", policy.maxAttempts(), "error", ex.getMessage());
                if (policy.delayMillis() > 0) {
                    sleep(policy.delayMillis());
                }
            }
        }
        throw last != null ? last : new IllegalStateException("Retry exhausted with no exception");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry sleep interrupted", e);
        }
    }
}
