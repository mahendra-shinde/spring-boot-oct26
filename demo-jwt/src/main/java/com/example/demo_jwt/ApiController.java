package com.example.demo_jwt;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/accounts/me")
    public Map<String, Object> myAccount(
            Authentication authentication) {

        return Map.of(
            "username", authentication.getName(),
            "accountNumber", "DEMO-10001",
            "accountType", "Savings",
            "balance", 25000.00,
            "currency", "INR"
        );
    }

    @GetMapping("/admin/report")
    public Map<String, Object> adminReport(
            Authentication authentication) {

        return Map.of(
            "message", "Welcome to the admin report",
            "requestedBy", authentication.getName(),
            "totalCustomers", 120
        );
    }

    @GetMapping("/public/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}