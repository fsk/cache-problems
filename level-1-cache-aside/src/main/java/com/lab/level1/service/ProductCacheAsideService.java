package com.lab.level1.service;

import com.lab.level1.cache.ProductRedisCache;
import com.lab.level1.config.Level1Properties;
import com.lab.level1.domain.Product;
import com.lab.level1.dto.CachedProduct;
import com.lab.level1.dto.ProductResponse;
import com.lab.level1.logging.DbQueryLogger;
import com.lab.level1.logging.Level1Log;
import com.lab.level1.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Naive cache-aside — no stampede protection, no warming, no invalidation on write.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheAsideService {

    private final ProductRedisCache cache;
    private final ProductRepository repository;
    private final Level1Properties properties;

    public Optional<ProductResponse> getById(long productId) {
        Optional<CachedProduct> cached = cache.get(productId);
        if (cached.isPresent()) {
            return Optional.of(toResponse(cached.get(), "CACHE"));
        }

        log.info("{} - loading from database, productId={}", Level1Log.CACHE_MISS, productId);

        Optional<Product> fromDb = DbQueryLogger.execute(
                "ProductRepository.findById id=" + productId,
                () -> loadFromDbWithSimulatedLatency(productId));

        if (fromDb.isEmpty()) {
            return Optional.empty();
        }

        CachedProduct cachedProduct = toCachedProduct(fromDb.get());
        cache.put(productId, cachedProduct);
        return Optional.of(toResponse(cachedProduct, "DATABASE"));
    }

    private Optional<Product> loadFromDbWithSimulatedLatency(long productId) {
        int latencyMs = properties.db().simulatedLatencyMs();
        if (latencyMs > 0) {
            try {
                Thread.sleep(latencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return repository.findById(productId);
    }

    public void evictCache(long productId) {
        cache.evict(productId);
    }

    private CachedProduct toCachedProduct(Product product) {
        return new CachedProduct(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getPopularityScore(),
                product.getUpdatedAt());
    }

    private ProductResponse toResponse(CachedProduct cached, String servedFrom) {
        return new ProductResponse(
                cached.id(),
                cached.name(),
                cached.sku(),
                cached.price(),
                cached.popularityScore(),
                cached.updatedAt(),
                servedFrom);
    }
}
