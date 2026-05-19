package com.lab.distributed.logging;

/**
 * Standard log tags for grep-friendly problem reproduction across all levels.
 * Format in logs: [TAG] message key=value
 */
public final class LabLog {

    private LabLog() {}

    public static final String CACHE_HIT = "[CACHE HIT]";
    public static final String CACHE_MISS = "[CACHE MISS]";
    public static final String CACHE_PUT = "[CACHE PUT]";
    public static final String CACHE_EVICT = "[CACHE EVICT]";
    public static final String DB_QUERY = "[DB QUERY EXECUTED]";
    public static final String STALE_CACHE = "[STALE CACHE DETECTED]";
    public static final String CACHE_STAMPEDE = "[CACHE STAMPEDE DETECTED]";
    public static final String HOT_KEY_PRESSURE = "[HOT KEY PRESSURE]";
    public static final String CONCURRENT_UPDATE = "[CONCURRENT UPDATE DETECTED]";
    public static final String OVERSOLD = "[OVERSOLD PRODUCT]";
    public static final String DUPLICATE_ORDER = "[DUPLICATE ORDER CREATED]";
    public static final String RACE_CONDITION = "[RACE CONDITION TRIGGERED]";
    public static final String EVENTUAL_CONSISTENCY_LAG = "[EVENTUAL CONSISTENCY LAG]";
    public static final String LOCK_SKIPPED = "[LOCK NOT ACQUIRED]";
    public static final String RETRY_ATTEMPT = "[RETRY ATTEMPT]";
}
