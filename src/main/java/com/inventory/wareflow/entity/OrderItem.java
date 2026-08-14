package com.inventory.wareflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single line item within an Order - one Product, a quantity, and the
 * price at time of purchase (captured separately from Product.price so
 * historical orders aren't affected by later price changes).
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "order_items")
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
public class OrderItem {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many order items can belong to the same order.
    @JoinColumn(name = "order_id", nullable = false)
    // @JoinColumn names the FK column stored on this table - this is the
    // owning side that Order.items refers to via mappedBy = "order".
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many order items can reference the same product.
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    // precision/scale define the decimal storage shape (10 digits total, 2 after
    // the point).
    private BigDecimal unitPriceAtPurchase;
    // Snapshot of Product.price at the moment this order was placed.
}