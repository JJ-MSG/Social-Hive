package com.example.socialhive.controller;
import com.example.socialhive.enums.*;

import com.example.socialhive.service.UserService;
import com.fasterxml.classmate.AnnotationOverrides;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.socialhive.service.TwitterOAuthService;
import com.example.socialhive.service.InstagramOAuthService;
import com.example.socialhive.entity.User;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.dto.AuthResponse;
import com.example.socialhive.dto.SocialAccountResponse;
import com.example.socialhive.repository.SocialAccountRepository;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.socialhive.util.PkceUtils;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final TwitterOAuthService twitterOAuthService;
    private final InstagramOAuthService instagramOAuthService;
    private final UserService userService;
    private final SocialAccountRepository accountRepository;


    @PostMapping("/connect/twitter")
    public ResponseEntity<AuthResponse> connectTwitter(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        String authUrl = twitterOAuthService.getAuthorizationUrl(user.getId());

        return ok(new AuthResponse(authUrl));
    }

    @PostMapping("/connect/instagram")
    public ResponseEntity<AuthResponse> connectInstagram(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        String authUrl = instagramOAuthService.getAuthorizationUrl(user.getId());

        return ok(new AuthResponse(authUrl));
    }

    @GetMapping("/callback/twitter")
    public ResponseEntity<String> twitterCallback(@RequestParam String code, @RequestParam String state) {
        SocialAccount account = twitterOAuthService.handleCallback(code, state);
        return ok("Twitter account connected successfully: " + account.getAccountName());
    }

    @GetMapping("/callback/instagram")
    public ResponseEntity<String> instagramCallback(@RequestParam String code, @RequestParam String state) {
        SocialAccount account = instagramOAuthService.handleCallback(code, state);
        return ok("Instagram account connected successfully: " + account.getAccountName());
    }

    @GetMapping
    public ResponseEntity<List<SocialAccountResponse>> getAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        List<SocialAccount> accounts = accountRepository.findByUserId(user.getId());

        return ok(accounts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> disconnectAccount(@PathVariable Long accountId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        // Verify ownership and delete
        accountRepository.deleteById(accountId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/start/instagram")
    public ResponseEntity<Void> startInstagramOAuth(HttpSession session) {
        String state = PkceUtils.randomState();
        session.setAttribute("IG_STATE", state);

        String authUrl = UriComponentsBuilder.fromUri(URI.create("https://www.facebook.com/dialog/oauth"))
                .queryParam("client_id", "2129253507609114")
                .queryParam("redirect_uri", "http://localhost:8080/api/accounts/callback/instagram")
                .queryParam("scope", "business_management,instagram_manage_comments,pages_show_list")
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authUrl))
                .build();
    }


    private SocialAccountResponse toResponse(SocialAccount account) {
        SocialAccountResponse response = new SocialAccountResponse();
        response.setId(account.getId());
        response.setPlatform(String.valueOf(account.getPlatform()));
        response.setAccountName(account.getAccountName());
        response.setAccountId(account.getAccountId());
        // Add other fields as needed
        return response;
    }





}
