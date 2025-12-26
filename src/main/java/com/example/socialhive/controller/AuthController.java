package com.example.socialhive.controller;

import com.example.socialhive.dto.AuthResponse;
import com.example.socialhive.dto.LoginRequest;
import com.example.socialhive.dto.RegisterRequest;
import com.example.socialhive.dto.UserResponse;
import com.example.socialhive.service.AuthenticationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<@NonNull AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<@NonNull AuthResponse> refresh(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @GetMapping("/me")
    public ResponseEntity<@NonNull UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                authService.getCurrentUser(userDetails.getUsername())
        );
    }
}
