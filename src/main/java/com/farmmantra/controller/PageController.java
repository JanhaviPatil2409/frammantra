package com.farmmantra.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login-page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register-page")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/advisory")
    public String advisoryPage() {
        return "advisory";
    }

    @GetMapping("/planner")
    public String plannerPage() {
        return "planner";
    }

    @GetMapping("/marketplace")
    public String marketplacePage() {
        return "marketplace";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/disease")
    public String diseasePage() {
        return "disease";
    }

    @GetMapping("/")
    public String home() {
    return "redirect:/login-page";
    }
}