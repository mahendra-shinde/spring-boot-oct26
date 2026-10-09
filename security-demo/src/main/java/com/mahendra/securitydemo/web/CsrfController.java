package com.mahendra.securitydemo.web;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Lets the HTML login page fetch a CSRF token to submit with the login/logout requests. */
@RestController
public class CsrfController {

    @GetMapping("/api/auth/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("headerName", token.getHeaderName());
        body.put("parameterName", token.getParameterName());
        body.put("token", token.getToken());
        return body;
    }
}
