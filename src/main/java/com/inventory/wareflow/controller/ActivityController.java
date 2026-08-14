package com.inventory.wareflow.controller;

import com.inventory.wareflow.enums.Activity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public lookup endpoint - lists every definable Activity permission type,
 * so the frontend's permission-assignment UI knows what checkboxes to render.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @GetMapping
    public Activity[] listActivities() {
        return Activity.values();
    }
}