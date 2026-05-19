package com.lab.distributed.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Minimal cache abstraction — intentionally does NOT include stampede protection,
 * distributed locks, or invalidation strategies. Levels implement broken patterns on top.
 */
public interface LabCache {

    Optional<String> get(String key);

    void put(String key, String value, Duration ttl);

    void delete(String key);

    boolean exists(String key);

    /** Full key with lab prefix applied */
    String resolveKey(String rawKey);
}
