package com.example.socialhive.controller;

import com.example.socialhive.service.TwitterOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/oauth/x")
@RequiredArgsConstructor
public class TwitterAuthController {

    private final TwitterOAuthService twitterOAuthService;

    // Redirect user to X authorization page
    @GetMapping("/authorize")
    public void authorize(
            @RequestParam Long userId,
            HttpServletResponse response
    ) throws IOException {

        String authorizationUrl = twitterOAuthService.getAuthorizationUrl(userId);
        response.sendRedirect(authorizationUrl);
    }

    // X redirects back here
    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        twitterOAuthService.handleCallback(code, state);
        return ResponseEntity.ok("Twitter account connected successfully");
    }
}
