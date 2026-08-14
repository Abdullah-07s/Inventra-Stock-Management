package com.inventory.wareflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks the on-hand quantity of a single Product at a single warehouse
 * location. A product can have multiple StockRecord rows if stocked across
 * more than one warehouse - the pair (product, warehouseLocation) is unique.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "stock_records", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id",
        "warehouse_location" }))
// @Table adds a composite unique constraint so the same product can't have
// two separate stock rows for the same warehouse location.
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
public class StockRecord {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many stock records can point to the same product
    // (one per warehouse it's stocked in). FetchType.LAZY avoids loading
    // the full Product unless explicitly accessed.
    @JoinColumn(name = "product_id", nullable = false)
    // @JoinColumn names the FK column stored on this table.
    private Product product;

    @Column(name = "warehouse_location", nullable = false, length = 100)
    // Identifies which warehouse this quantity is held at (e.g. "Warehouse-A").
    private String warehouseLocation;

    @Column(nullable = false)
    @Builder.Default
    // @Builder.Default ensures this default is applied even when using the builder.
    private Integer quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();
    // Updated every time quantity changes - useful for audit/staleness checks.
}