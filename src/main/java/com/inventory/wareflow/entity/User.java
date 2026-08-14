package com.inventory.wareflow.entity;

import com.inventory.wareflow.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a registered account in the system.
 * Password is stored as a jBCrypt hash - never plaintext.
 * Role determines the base access tier (USER / ADMIN / SUPERADMIN);
 * fine-grained ADMIN permissions live separately in AdminPermission.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "users")
// @Table sets the actual table name - "users" instead of the reserved word "user".
@Data
// @Data generates getters, setters, toString, equals, and hashCode.
@NoArgsConstructor
// @NoArgsConstructor generates a public no-arg constructor, required by JPA/Hibernate.
@AllArgsConstructor
// @AllArgsConstructor generates a constructor with all fields - useful with @Builder.
@Builder
// @Builder generates a fluent builder pattern for clean object construction.
public class User {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL AUTO_INCREMENT).
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    // @Column customizes the DB column - required, unique, max length 100.
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String passwordHash;
    // Stores the jBCrypt hash output, never the raw password.

    @Enumerated(EnumType.STRING)
    // @Enumerated(STRING) stores the enum's name (e.g. "ADMIN") instead of its
    // ordinal index, so the DB stays readable and safe if enum order ever changes.
    @Column(nullable = false)
    @Builder.Default
    // @Builder.Default ensures this default is applied even when using the builder.
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    // updatable = false locks this field after initial insert.
}