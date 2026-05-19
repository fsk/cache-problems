package com.lab.distributed.util;

/**
 * Intentionally slows DB reads so cache miss / stampede effects are visible in logs.
 */
public final class SlowQuerySimulator {

    private SlowQuerySimulator() {}

    public static void simulateDbLatency(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
