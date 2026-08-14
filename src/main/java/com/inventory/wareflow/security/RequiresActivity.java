package com.inventory.wareflow.security;

import com.inventory.wareflow.enums.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as requiring a specific Activity permission.
 * Enforcement happens in ActivityAuthorizationAspect via Spring AOP -
 * NOT Spring Security. SUPERADMIN always passes regardless of the value
 * here; ADMIN passes only if explicitly granted this Activity; USER never
 * passes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresActivity {
    Activity value();
}