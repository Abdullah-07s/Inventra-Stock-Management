package com.inventory.wareflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single order (incoming restock or outgoing customer order),
 * placed by a User. Line items live in OrderItem; this entity holds the
 * order-level metadata and status.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "orders")
// @Table sets the explicit table name - "orders" avoids clashing with the
// reserved SQL word "order".
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
public class Order {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many orders can be placed by the same user.
    // FetchType.LAZY avoids loading the full User unless explicitly accessed.
    @JoinColumn(name = "user_id", nullable = false)
    // @JoinColumn names the FK column stored on this table.
    private User user;

    @Enumerated(EnumType.STRING)
    // @Enumerated(STRING) stores the enum's name (e.g. "PENDING") instead of
    // its ordinal index, keeping the DB readable.
    @Column(nullable = false)
    @Builder.Default
    // @Builder.Default ensures this default is applied even when using the builder.
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    // updatable = false locks this field after initial insert.

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    // @OneToMany: one order can have many line items. mappedBy points to the
    // "order" field on OrderItem (the owning side of the relationship).
    // cascade = ALL means saving/deleting an Order also saves/deletes its items.
    // orphanRemoval = true deletes an item automatically if removed from this list.
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Order lifecycle status, updated via PUT /api/orders/{id}/status.
     */
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        SHIPPED,
        COMPLETED,
        CANCELLED
    }
}