package com.inventory.wareflow.security;

import com.inventory.wareflow.entity.User;
import com.inventory.wareflow.enums.Role;
import com.inventory.wareflow.exception.AuthException;
import com.inventory.wareflow.exception.ForbiddenException;
import com.inventory.wareflow.repository.AdminPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Intercepts every @RequiresActivity-annotated method BEFORE it executes
 * and enforces the role/activity hierarchy:
 * - No authenticated user (AuthContext empty) -> 401
 * - SUPERADMIN -> always passes
 * - ADMIN with the required Activity granted -> passes
 * - ADMIN without the required Activity granted -> 403
 * - USER -> always 403
 *
 * This is the manual replacement for what Spring Security's
 * 
 * @PreAuthorize/method security would normally do.
 */
@Aspect
// @Aspect marks this class as containing cross-cutting interception logic.
@Component
// @Component registers it as a Spring bean so the AOP proxy machinery picks it
// up.
@RequiredArgsConstructor
public class ActivityAuthorizationAspect {

    private final AdminPermissionRepository adminPermissionRepository;

    @Before("@annotation(com.inventory.wareflow.security.RequiresActivity)")
    // @Before runs this method before any @RequiresActivity-annotated method
    // executes.
    // The pointcut expression matches by annotation presence, not by package/class
    // name.
    public void checkActivity(JoinPoint joinPoint) {
        User currentUser = AuthContext.getCurrentUser();

        if (currentUser == null) {
            throw new AuthException("Authentication required", HttpStatus.UNAUTHORIZED);
        }

        if (currentUser.getRole() == Role.SUPERADMIN) {
            return;
            // SUPERADMIN auto-passes every activity check, per the role hierarchy spec.
        }

        if (currentUser.getRole() == Role.USER) {
            throw new ForbiddenException("This action requires admin privileges");
        }

        // At this point the role is ADMIN - check for the specific granted activity.
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresActivity annotation = method.getAnnotation(RequiresActivity.class);

        boolean hasPermission = adminPermissionRepository
                .existsByUserAndActivity(currentUser, annotation.value());

        if (!hasPermission) {
            throw new ForbiddenException(
                    "You do not have the '" + annotation.value() + "' permission required for this action");
        }
    }
}