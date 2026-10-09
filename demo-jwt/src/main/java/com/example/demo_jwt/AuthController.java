package com.example.demo_jwt;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-seconds:900}") long expirationSeconds) {

        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public record LoginRequest(String username, String password) {
    }

    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn) {
    }

    @PostMapping(value="/login", consumes="application/json")
    /*
     * What happens during login?
     * 
     * The client sends credentials to /api/auth/login.
     * 
     * AuthenticationManager validates those credentials.
     * 
     * The controller extracts the authenticated user's roles.
     * 
     * JwtEncoder creates a signed token with a subject, roles, issue time, expiry
     * time, and issuer.
     * 
     * The token is returned to the client.
     */
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest request) {
                
        System.out.println("Login attempted: "+ request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        Instant now = Instant.now();

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("jwt-security-demo")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return ResponseEntity.ok(
                new TokenResponse(token, "Bearer",
                        expirationSeconds));
    }
}