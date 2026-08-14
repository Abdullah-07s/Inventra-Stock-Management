package com.inventory.wareflow.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Thrown for authentication failures: bad credentials, duplicate username/email
 * on registration, etc. Carries its own HttpStatus so GlobalExceptionHandler
 * can respond with the right code (400 for duplicates, 401 for bad login).
 */
@Getter
public class AuthException extends RuntimeException {
    private final HttpStatus status;

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}