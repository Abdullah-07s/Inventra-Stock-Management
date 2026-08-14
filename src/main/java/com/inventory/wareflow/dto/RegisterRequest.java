package com.inventory.wareflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/register.
 * All new registrations default to USER role - promotion to ADMIN happens
 * separately via the SUPERADMIN-only /api/admins/{userId}/promote endpoint.
 */
@Data
// @Data generates getters, setters, toString, equals, and hashCode.
@NoArgsConstructor
// @NoArgsConstructor generates a public no-arg constructor - required for
// Jackson to deserialize incoming JSON into this DTO.
@AllArgsConstructor
// @AllArgsConstructor generates a constructor with all fields.
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    // @NotBlank rejects null, empty, or whitespace-only values.
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    // @Email validates the string matches a standard email pattern.
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}