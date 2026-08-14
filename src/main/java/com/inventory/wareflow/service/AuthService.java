package com.inventory.wareflow.service;

import com.inventory.wareflow.dto.AuthResponse;
import com.inventory.wareflow.dto.LoginRequest;
import com.inventory.wareflow.dto.RegisterRequest;
import com.inventory.wareflow.entity.User;
import com.inventory.wareflow.enums.Role;
import com.inventory.wareflow.exception.AuthException;
import com.inventory.wareflow.repository.UserRepository;
import com.inventory.wareflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Handles registration and login. Password hashing/verification uses jBCrypt
 * directly (standalone, not via Spring Security) - this is the manual
 * replacement for what Spring Security's PasswordEncoder would normally do.
 */
@Service
// @Service marks this as a Spring-managed business-logic bean.
@RequiredArgsConstructor
// @RequiredArgsConstructor generates a constructor for all final fields -
// Spring injects userRepository and jwtUtil automatically.
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username is already taken", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        String passwordHash = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        // BCrypt.gensalt() generates a random salt baked into the hash output itself -
        // no need to store the salt separately.

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .role(Role.USER)
                // All new registrations start as USER - promotion to ADMIN is a
                // separate SUPERADMIN-only action (Phase 4).
                .build();

        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getId(), saved.getUsername(), saved.getRole());

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .username(saved.getUsername())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        boolean passwordMatches = BCrypt.checkpw(request.getPassword(), user.getPasswordHash());
        // BCrypt.checkpw compares the raw password against the stored hash -
        // extracts the salt from the hash itself, so no separate salt lookup needed.

        if (!passwordMatches) {
            // Deliberately the same error message as "user not found" above -
            // avoids leaking whether a given username exists in the system.
            throw new AuthException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}