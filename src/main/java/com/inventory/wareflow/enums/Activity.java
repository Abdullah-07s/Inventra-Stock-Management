package com.inventory.wareflow.enums;

/**
 * Fixed catalog of granular permission types that a SUPERADMIN can assign
 * to individual ADMIN accounts. An admin with no assigned activities has
 * no management access beyond what a standard USER can already do.
 */
public enum Activity {
    MANAGE_PRODUCTS,
    MANAGE_SUPPLIERS,
    MANAGE_ORDERS,
    MANAGE_STOCK,
    VIEW_REPORTS,
    MANAGE_ADMINS
}