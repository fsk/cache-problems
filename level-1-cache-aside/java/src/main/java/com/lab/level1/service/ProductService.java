package com.lab.level1.service;

import com.lab.level1.dto.ProductResponse;
import com.lab.level1.logging.CacheAccessContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade over {@link ProductCacheLoader} — adds {@code servedFrom} using {@link CacheAccessContext}.
 * Cache-aside via Spring {@link org.springframework.cache.annotation.Cacheable}.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCacheLoader cacheLoader;

    public Optional<ProductResponse> getById(long productId) {
        CacheAccessContext.clear();
        ProductResponse loaded = cacheLoader.load(productId);
        if (loaded == null) {
            return Optional.empty();
        }
        String servedFrom = CacheAccessContext.wasCacheHit() ? "CACHE" : "DATABASE";
        return Optional.of(loaded.withServedFrom(servedFrom));
    }

    @CacheEvict(cacheNames = "products", key = "#productId")
    public void evictCache(long productId) {
        // eviction + [CACHE EVICT] log handled by LoggingCache
    }
}
