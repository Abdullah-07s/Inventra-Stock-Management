package com.inventory.wareflow.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf page shells. All pages render a generic template
 * server-side; role-based content and data come from the REST API via
 * client-side JS (auth.js + dashboard.js), since auth is JWT-based rather
 * than session-based.
 */
@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}