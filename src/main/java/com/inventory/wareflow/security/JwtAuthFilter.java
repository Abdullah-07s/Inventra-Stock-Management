package com.inventory.wareflow.security;

import com.inventory.wareflow.entity.User;
import com.inventory.wareflow.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Manually intercepts every request, validates the Bearer JWT (if present),
 * and populates AuthContext with the authenticated User for downstream
 * controllers/services to read. This is a plain Servlet filter chain -
 * NOT Spring Security's filter chain - reinforcing the "no Spring Security"
 * architectural constraint of this project.
 *
 * Public endpoints (register/login) are explicitly skipped since they're
 * called before a token exists. Everything else passes through untouched
 * if no/invalid token is present - actual authorization (rejecting
 * unauthenticated requests) happens at the controller/service layer via
 * 
 * @RequiresActivity-style checks in Phase 4, not here.
 */
@Component
// @Component registers this as a Spring-managed bean, auto-detected and
// registered into the servlet filter chain by Spring Boot's default filter
// registration.
@RequiredArgsConstructor
// @RequiredArgsConstructor generates a constructor for jwtUtil and
// userRepository -
// Spring injects both automatically.
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this filter runs exactly once per request,
    // even in forward/include scenarios - a plain jakarta.servlet.Filter doesn't
    // guarantee that on its own.

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            if (PUBLIC_PATHS.contains(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // substring(7) strips the literal "Bearer " prefix (7 characters).

                try {
                    Claims claims = jwtUtil.validateAndParse(token);
                    Long userId = jwtUtil.extractUserId(claims);

                    Optional<User> userOpt = userRepository.findById(userId);
                    userOpt.ifPresent(AuthContext::setCurrentUser);
                    // If the user was deleted after the token was issued, we simply
                    // don't populate AuthContext - downstream checks will treat the
                    // request as unauthenticated.

                } catch (JwtException e) {
                    // Invalid/expired/malformed token - AuthContext stays empty.
                    // We don't reject the request here; that's the controller/service
                    // layer's job (Phase 4), keeping this filter single-purpose:
                    // "identify the caller if possible."
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            AuthContext.clear();
            // Always clear, even if an exception was thrown above - prevents
            // thread-local leakage into the next request on this pooled thread.
        }
    }
}