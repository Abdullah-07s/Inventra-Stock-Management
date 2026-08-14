package com.inventory.wareflow.security;

import com.inventory.wareflow.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Hand-rolled JWT utility: generates, signs, and validates tokens manually.
 * This replaces what Spring Security's JwtAuthenticationProvider/JwtDecoder
 * would normally do - a deliberate architectural choice for this project.
 *
 * Token payload (claims) carries just enough to authorize requests without
 * a DB hit on every call: userId, username, and role.
 */
@Component
// @Component registers this as a Spring-managed bean so it can be @Autowired
// elsewhere.
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        // Constructor injection pulls jwt.secret / jwt.expiration-ms from
        // application-local.properties (resolved via the "local" profile).
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        // hmacShaKeyFor builds a proper signing key from the raw secret bytes.
        // The secret must be long enough (256+ bits) or this throws WeakKeyException.
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT for the given user, embedding userId, username, and
     * role as claims.
     */
    public String generateToken(Long userId, String username, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates a token's signature and expiry. Returns the parsed claims if valid,
     * throws a JwtException subclass (ExpiredJwtException, SignatureException,
     * etc.) if not.
     */
    public Claims validateAndParse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public Role extractRole(Claims claims) {
        return Role.valueOf(claims.get("role", String.class));
    }
}