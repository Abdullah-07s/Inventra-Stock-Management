package com.inventory.wareflow.enums;

/**
 * Defines the three access tiers in the system.
 * - USER: standard scoped access (view products/stock, place orders)
 * - ADMIN: has only the specific activity-permissions granted by a SUPERADMIN (no blanket access)
 * - SUPERADMIN: full system access, plus the ability to promote users to ADMIN
 *   and assign/revoke granular activity-permissions per admin
 */
public enum Role {
    USER,
    ADMIN,
    SUPERADMIN
}