package com.lab.level2.service;

import com.lab.level2.domain.Product;
import com.lab.level2.dto.ProductResponse;
import com.lab.level2.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** {@link Cacheable} — method runs only on cache miss. */
@Service
@RequiredArgsConstructor
public class ProductCacheLoader {

    private static final Logger DB_LOG = LoggerFactory.getLogger("lab.db");

    private final ProductRepository repository;

    @Cacheable(cacheNames = "products", key = "#id", unless = "#result == null")
    public ProductResponse load(long id) {
        DB_LOG.info("[DB QUERY EXECUTED] productId={}", id);
        return repository.findById(id).map(this::toResponse).orElse(null);
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
