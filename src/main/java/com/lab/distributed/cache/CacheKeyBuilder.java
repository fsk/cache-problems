package com.lab.distributed.cache;

/**
 * Consistent cache key naming across levels.
 */
public final class CacheKeyBuilder {

    private CacheKeyBuilder() {}

    public static String product(long productId) {
        return "product:" + productId;
    }

    public static String productList(int page) {
        return "product:list:page:" + page;
    }

    public static String inventory(long productId) {
        return "inventory:" + productId;
    }

    public static String order(String orderId) {
        return "order:" + orderId;
    }

    public static String user(long userId) {
        return "user:" + userId;
    }

    public static String hotProduct(long productId) {
        return "hot:product:" + productId;
    }
}
