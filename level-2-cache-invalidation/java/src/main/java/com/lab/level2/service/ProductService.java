package com.lab.level2.service;

import com.lab.level2.domain.Product;
import com.lab.level2.dto.ProductResponse;
import com.lab.level2.dto.UpdatePriceRequest;
import com.lab.level2.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Update writes DB only — intentionally no {@code @CacheEvict} (stale cache lab).
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger CACHE_LOG = LoggerFactory.getLogger("lab.cache");

    private final ProductRepository repository;
    private final ProductCacheLoader cacheLoader;
    private final CacheManager cacheManager;
    private final Set<Long> staleProductIds = ConcurrentHashMap.newKeySet();

    public Optional<ProductResponse> getById(long id) {
        boolean cachedBefore = isCached(id);
        if (cachedBefore && staleProductIds.remove(id)) {
            CACHE_LOG.warn("[STALE CACHE DETECTED] productId={}", id);
        }
        ProductResponse loaded = cacheLoader.load(id);
        if (loaded == null) {
            return Optional.empty();
        }
        return Optional.of(loaded.withSource(cachedBefore ? "CACHE" : "DATABASE"));
    }

    @Transactional
    public Optional<ProductResponse> updatePrice(long id, UpdatePriceRequest request) {
        Optional<Product> product = repository.findById(id);
        if (product.isEmpty()) {
            return Optional.empty();
        }
        Product entity = product.get();
        entity.setPrice(request.price());
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);

        staleProductIds.add(id);
        CACHE_LOG.warn("[CACHE NOT INVALIDATED] productId={} newPrice={}", id, request.price());

        return Optional.of(toResponse(entity).withSource("DATABASE"));
    }

    private boolean isCached(long id) {
        Cache cache = cacheManager.getCache("products");
        if (cache == null) {
            return false;
        }
        Cache.ValueWrapper wrapper = cache.get(id);
        return wrapper != null && wrapper.get() != null;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getUpdatedAt(),
                null);
    }
}
