package com.inventory.wareflow.exception;

/**
 * Thrown when a lookup by ID (product, supplier, order, etc.) finds nothing.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}