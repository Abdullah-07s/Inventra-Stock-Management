package com.inventory.wareflow.dto;

import com.inventory.wareflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned by both register and login - carries the signed
 * JWT plus a small amount of user context so the frontend doesn't need a
 * separate call just to know who's logged in and what role they have.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private Role role;
}