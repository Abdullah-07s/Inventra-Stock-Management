package com.inventory.wareflow.controller;

import com.inventory.wareflow.dto.AdminSummaryResponse;
import com.inventory.wareflow.dto.PermissionUpdateRequest;
import com.inventory.wareflow.enums.Activity;
import com.inventory.wareflow.security.RequiresActivity;
import com.inventory.wareflow.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SUPERADMIN-only endpoints for managing admin accounts and their
 * granular activity permissions. Every method here requires MANAGE_ADMINS,
 * which only a SUPERADMIN can ever hold (no admin can be granted this
 * activity - enforced implicitly since only SUPERADMIN auto-passes,
 * and MANAGE_ADMINS is never assigned via updatePermission in practice
 * unless a SUPERADMIN explicitly chooses to - the spec allows this as a
 * design choice for that edge case).
 */
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    @RequiresActivity(Activity.MANAGE_ADMINS)
    public List<AdminSummaryResponse> listAdmins() {
        return adminService.listAdmins();
    }

    @PostMapping("/{userId}/promote")
    @RequiresActivity(Activity.MANAGE_ADMINS)
    public AdminSummaryResponse promote(@PathVariable Long userId) {
        return adminService.promoteToAdmin(userId);
    }

    @PutMapping("/{userId}/permissions")
    @RequiresActivity(Activity.MANAGE_ADMINS)
    public AdminSummaryResponse updatePermissions(
            @PathVariable Long userId,
            @Valid @RequestBody PermissionUpdateRequest request) {
        return adminService.updatePermission(userId, request);
    }
}