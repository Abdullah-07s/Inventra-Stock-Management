package com.inventory.wareflow.dto;

import com.inventory.wareflow.enums.Activity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/admins/{userId}/permissions.
 * "grant: true" adds the activity; "grant: false" revokes it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateRequest {

    @NotNull(message = "Activity is required")
    private Activity activity;

    @NotNull(message = "grant flag is required (true to assign, false to revoke)")
    private Boolean grant;
}