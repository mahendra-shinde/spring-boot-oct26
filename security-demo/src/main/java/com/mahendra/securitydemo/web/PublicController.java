package com.mahendra.securitydemo.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicController {

    @GetMapping("/api/public/hello")
    public Map<String, Object> hello() {
        return Map.of(
                "message", "Hello! This endpoint is public and requires no authentication.");
    }
}
