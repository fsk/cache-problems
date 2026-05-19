package com.lab.level1.logging;

/** Level-1 specific log tags — grep-friendly reproduction. */
public final class Level1Log {

    private Level1Log() {}

    public static final String CACHE_HIT = "[CACHE HIT]";
    public static final String CACHE_MISS = "[CACHE MISS]";
    public static final String CACHE_PUT = "[CACHE PUT]";
    public static final String CACHE_EVICT = "[CACHE EVICT]";
    public static final String DB_QUERY = "[DB QUERY EXECUTED]";
}
