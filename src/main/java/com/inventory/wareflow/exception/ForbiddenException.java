package com.inventory.wareflow.exception;

/**
 * Thrown when an authenticated user is correctly identified but lacks the
 * required role or activity-permission for the action they're attempting.
 * Used heavily in Phase 4's @RequiresActivity enforcement.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}