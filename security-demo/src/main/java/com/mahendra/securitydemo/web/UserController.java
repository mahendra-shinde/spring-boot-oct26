package com.mahendra.securitydemo.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/user/profile")
    public Map<String, Object> profile(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", authentication.getName());
        body.put("roles", roles);
        return body;
    }
}
