package com.inventory.wareflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A sellable product in the catalog. Each product belongs to one Category
 * and is sourced from one Supplier. Actual on-hand quantity lives in
 * StockRecord, not here - this entity is catalog/pricing data only.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "products")
// @Table sets the explicit table name.
@Data
// @Data generates getters, setters, toString, equals, and hashCode.
@NoArgsConstructor
// @NoArgsConstructor generates a public no-arg constructor, required by
// JPA/Hibernate.
@AllArgsConstructor
// @AllArgsConstructor generates a constructor with all fields - useful with
// @Builder.
@Builder
// @Builder generates a fluent builder pattern for clean object construction.
public class Product {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    // @Column: SKU is the unique catalog identifier for the product.
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    // precision/scale define the decimal storage shape (10 digits total, 2 after
    // the point).
    private BigDecimal price;
    // BigDecimal is used instead of double/float to avoid floating-point rounding
    // errors in money.

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many products can belong to the same category.
    // FetchType.LAZY avoids loading the full Category unless explicitly accessed.
    @JoinColumn(name = "category_id", nullable = false)
    // @JoinColumn names the FK column stored on this table.
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many products can come from the same supplier.
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    // @Builder.Default ensures this default is applied even when using the builder.
    private LocalDateTime createdAt = LocalDateTime.now();
    // updatable = false locks this field after initial insert.
}