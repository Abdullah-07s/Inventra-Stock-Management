package com.inventory.wareflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A supplier/vendor that provides products to the warehouse.
 * Since the seed dataset doesn't include native supplier records,
 * these are derived from product/brand data during seeding.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "suppliers")
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
public class Supplier {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    // @Column: supplier/brand names must be unique and non-null.
    private String name;

    @Column(length = 150)
    private String contactEmail;

    @Column(length = 30)
    private String contactPhone;

    @Column(length = 255)
    private String address;
}