package com.inventory.wareflow.controller;

import com.inventory.wareflow.dto.ProductRequest;
import com.inventory.wareflow.entity.Product;
import com.inventory.wareflow.enums.Activity;
import com.inventory.wareflow.security.RequiresActivity;
import com.inventory.wareflow.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET endpoints are open to any authenticated user (matches spec: USER can
 * "view products/stock"). Write operations require MANAGE_PRODUCTS.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> listAll() {
        return productService.listAll();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @RequiresActivity(Activity.MANAGE_PRODUCTS)
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        Product created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @RequiresActivity(Activity.MANAGE_PRODUCTS)
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresActivity(Activity.MANAGE_PRODUCTS)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}