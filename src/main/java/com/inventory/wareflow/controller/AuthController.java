package com.inventory.wareflow.controller;

import com.inventory.wareflow.dto.AuthResponse;
import com.inventory.wareflow.dto.LoginRequest;
import com.inventory.wareflow.dto.RegisterRequest;
import com.inventory.wareflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public authentication endpoints - both are listed in JwtAuthFilter's
 * PUBLIC_PATHS since a token obviously can't exist before login/register
 * succeeds.
 */
@RestController
// @RestController combines @Controller + @ResponseBody - return values are
// serialized directly to JSON rather than resolved as view names.
@RequestMapping("/api/auth")
// @RequestMapping sets the base path for every endpoint in this controller.
@RequiredArgsConstructor
// @RequiredArgsConstructor generates a constructor for the final authService
// field.
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // @Valid triggers Bean Validation on the DTO - failures are caught by
        // GlobalExceptionHandler's MethodArgumentNotValidException handler.
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}