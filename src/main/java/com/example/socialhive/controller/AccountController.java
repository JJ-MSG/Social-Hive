package com.example.socialhive.controller;

import com.example.socialhive.dto.OAuthResponse;
import com.example.socialhive.dto.SocialAccountResponse;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.entity.User;
import com.example.socialhive.repository.SocialAccountRepository;
import com.example.socialhive.repository.UserRepository;
import com.example.socialhive.service.TwitterOAuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final SocialAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TwitterOAuthService twitterOAuthService;
//    private final InstagramOAuthService instagramOAuthService;

    @GetMapping("/connect/twitter")
    public ResponseEntity<OAuthResponse> connectTwitter(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        String authUrl = twitterOAuthService.getAuthorizationUrl(user.getId());

        return ResponseEntity.ok(new OAuthResponse(authUrl));
    }

//    @PostMapping("/connect/instagram")
//    public ResponseEntity<OAuthResponse> connectInstagram(@AuthenticationPrincipal UserDetails userDetails) {
//        User user = getUserFromDetails(userDetails);
//        String authUrl = instagramOAuthService.getAuthorizationUrl(user.getId());
//
//        return ResponseEntity.ok(new OAuthResponse(authUrl));
//    }

    @GetMapping("/callback/twitter")
    public ResponseEntity<String> twitterCallback(@RequestParam String code, @RequestParam String state) {
        SocialAccount account = twitterOAuthService.handleCallback(code, state);
        return ResponseEntity.ok("Twitter account connected successfully: " + account.getAccountName());
    }

//    @GetMapping("/callback/instagram")
//    public ResponseEntity<String> instagramCallback(@RequestParam String code, @RequestParam String state) {
//        SocialAccount account = instagramOAuthService.handleCallback(code, state);
//        return ResponseEntity.ok("Instagram account connected successfully: " + account.getAccountName());
//    }

    @GetMapping
    public ResponseEntity<List<SocialAccountResponse>> getAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<SocialAccount> accounts = accountRepository.findAllByUser(user);

        return ResponseEntity.ok(accounts.stream()
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

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private SocialAccountResponse toResponse(SocialAccount a) {
        return new SocialAccountResponse(
                a.getId(),
                a.getPlatform().name(),
                a.getAccountName(),
                a.getAccountId(),
                a.getProfileImageUrl(),
                a.getFollowersCount()
        );
    }
}