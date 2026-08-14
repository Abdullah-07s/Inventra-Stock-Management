package com.inventory.wareflow.security;

import com.inventory.wareflow.entity.User;

/**
 * Holds the authenticated User for the duration of the current request thread.
 * Set by JwtAuthFilter after successful token validation, cleared in its
 * finally block. Controllers/services call AuthContext.getCurrentUser()
 * anywhere in the call stack instead of threading the user through every
 * method signature.
 *
 * IMPORTANT: must always be cleared after each request (see JwtAuthFilter),
 * otherwise a pooled servlet thread could leak one user's identity into
 * the next unrelated request.
 */
public class AuthContext {

    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
        // Private constructor - this is a static-only utility class, never
        // instantiated.
    }

    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
        // .remove() rather than .set(null) - fully detaches the value so the
        // thread-local map entry doesn't linger, which matters in pooled threads.
    }
}