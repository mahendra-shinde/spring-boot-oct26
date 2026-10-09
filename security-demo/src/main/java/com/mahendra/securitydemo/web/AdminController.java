package com.mahendra.securitydemo.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AdminController {

    @GetMapping("/api/admin/data")
    public Map<String, Object> adminData() {
        return Map.of(
                "message", "Confidential admin data",
                "items", List.of("report-1", "report-2", "report-3"));
    }
}
