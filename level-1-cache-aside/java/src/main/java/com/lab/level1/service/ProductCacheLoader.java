package com.lab.level1.service;

import com.lab.level1.config.Level1Properties;
import com.lab.level1.domain.Product;
import com.lab.level1.dto.ProductResponse;
import com.lab.level1.logging.DbQueryLogger;
import com.lab.level1.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * {@link Cacheable} entry point — method body runs only on cache miss (lazy load from DB).
 */
@Service
@RequiredArgsConstructor
public class ProductCacheLoader {

    private final ProductRepository repository;
    private final Level1Properties properties;
    private final DbQueryLogger dbQueryLogger;

    @Cacheable(cacheNames = "products", key = "#productId", unless = "#result == null")
    public ProductResponse load(long productId) {
        return dbQueryLogger.execute("ProductRepository.findById id=" + productId, () -> loadFromDbWithSimulatedLatency(productId));
    }

    private ProductResponse loadFromDbWithSimulatedLatency(long productId) {
        int latencyMs = properties.db().simulatedLatencyMs();
        if (latencyMs > 0) {
            try {
                Thread.sleep(latencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return repository.findById(productId)
                .map(this::toResponse)
                .orElse(null);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getPopularityScore(),
                product.getUpdatedAt(),
                null);
    }
}
