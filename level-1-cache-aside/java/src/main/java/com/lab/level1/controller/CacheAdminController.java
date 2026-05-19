package com.lab.level1.controller;

import com.lab.level1.dto.ApiResponse;
import com.lab.level1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates TTL expiry / manual eviction — next GET will be cache miss + DB query.
 */
@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
public class CacheAdminController {

    private final ProductService productService;

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> evictProduct(@PathVariable long id) {
        productService.evictCache(id);
        return ResponseEntity.ok(ApiResponse.ok("Cache evicted for product " + id));
    }
}
