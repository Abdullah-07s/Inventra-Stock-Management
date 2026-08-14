package com.inventory.wareflow.dto;

import com.inventory.wareflow.enums.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response shape for GET /api/admins - one admin plus their granted activities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSummaryResponse {
    private Long userId;
    private String username;
    private String email;
    private List<Activity> grantedActivities;
}