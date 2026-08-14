package com.inventory.wareflow.service;

import com.inventory.wareflow.dto.AdminSummaryResponse;
import com.inventory.wareflow.dto.PermissionUpdateRequest;
import com.inventory.wareflow.entity.AdminPermission;
import com.inventory.wareflow.entity.User;
import com.inventory.wareflow.enums.Role;
import com.inventory.wareflow.exception.AuthException;
import com.inventory.wareflow.exception.ResourceNotFoundException;
import com.inventory.wareflow.repository.AdminPermissionRepository;
import com.inventory.wareflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SUPERADMIN-only operations: listing admins with their permissions,
 * promoting a USER to ADMIN, and granting/revoking specific Activity
 * permissions per admin. Enforcement of "SUPERADMIN only" happens via
 * 
 * @RequiresActivity(MANAGE_ADMINS) on the controller methods that call this.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AdminPermissionRepository adminPermissionRepository;

    public List<AdminSummaryResponse> listAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .map(admin -> AdminSummaryResponse.builder()
                        .userId(admin.getId())
                        .username(admin.getUsername())
                        .email(admin.getEmail())
                        .grantedActivities(
                                adminPermissionRepository.findByUser(admin).stream()
                                        .map(AdminPermission::getActivity)
                                        .toList())
                        .build())
                .toList();
    }

    public AdminSummaryResponse promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.USER) {
            throw new AuthException(
                    "Only USER accounts can be promoted - this account is already " + user.getRole(),
                    HttpStatus.BAD_REQUEST);
        }

        user.setRole(Role.ADMIN);
        User saved = userRepository.save(user);

        return AdminSummaryResponse.builder()
                .userId(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .grantedActivities(List.of())
                // Newly promoted admins start with zero granted activities -
                // matches the spec's "no admin has blanket access by default".
                .build();
    }

    public AdminSummaryResponse updatePermission(Long userId, PermissionUpdateRequest request) {
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (admin.getRole() != Role.ADMIN) {
            throw new AuthException(
                    "Permissions can only be assigned to ADMIN accounts - this account is " + admin.getRole(),
                    HttpStatus.BAD_REQUEST);
        }

        boolean alreadyGranted = adminPermissionRepository
                .existsByUserAndActivity(admin, request.getActivity());

        if (request.getGrant()) {
            if (!alreadyGranted) {
                adminPermissionRepository.save(
                        AdminPermission.builder()
                                .user(admin)
                                .activity(request.getActivity())
                                .build());
            }
        } else {
            if (alreadyGranted) {
                adminPermissionRepository.deleteByUserAndActivity(admin, request.getActivity());
            }
        }

        return AdminSummaryResponse.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .grantedActivities(
                        adminPermissionRepository.findByUser(admin).stream()
                                .map(AdminPermission::getActivity)
                                .toList())
                .build();
    }
}