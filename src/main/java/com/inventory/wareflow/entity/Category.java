package com.inventory.wareflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A product category (e.g. "Footwear", "Camping Gear").
 * Simple lookup entity - one category can have many products.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "categories")
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
public class Category {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    // @Column: category names must be unique and non-null.
    private String name;

    @Column(length = 500)
    private String description;
    // Optional short description of what belongs in this category.
}