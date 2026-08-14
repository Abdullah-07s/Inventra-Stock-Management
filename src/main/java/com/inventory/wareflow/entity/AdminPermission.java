package com.inventory.wareflow.entity;

import com.inventory.wareflow.enums.Activity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Join entity representing a single granted permission: one row = one
 * (admin, activity) pair. An admin with zero rows here has no management
 * access beyond a standard USER. Only a SUPERADMIN can create/delete these
 * rows.
 */
@Entity
// @Entity marks this class as a JPA-managed database table.
@Table(name = "admin_permissions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "activity" }))
// @Table adds a composite unique constraint so the same admin can't have
// the same activity granted twice (prevents duplicate rows).
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
public class AdminPermission {

    @Id
    // @Id marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue delegates ID generation to the database (MySQL
    // AUTO_INCREMENT).
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne: many AdminPermission rows can point to the same User.
    // FetchType.LAZY avoids loading the full User unless explicitly accessed.
    @JoinColumn(name = "user_id", nullable = false)
    // @JoinColumn names the FK column stored on this table.
    private User user;

    @Enumerated(EnumType.STRING)
    // @Enumerated(STRING) stores the enum's name (e.g. "MANAGE_PRODUCTS")
    // instead of its ordinal index, keeping the DB readable.
    @Column(nullable = false)
    private Activity activity;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime grantedAt = LocalDateTime.now();
    // Timestamp of when this permission was assigned - useful for audit history.
}