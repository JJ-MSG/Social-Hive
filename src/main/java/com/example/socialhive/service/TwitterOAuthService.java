package com.example.socialhive.service;

import com.example.socialhive.dto.TwitterAccessToken;
import com.example.socialhive.dto.TwitterUserProfile;
import com.example.socialhive.dto.TwitterUserResponse;
import com.example.socialhive.entity.*;
import com.example.socialhive.enums.Platform;
import com.example.socialhive.exception.ResourceNotFoundException;
import com.example.socialhive.repository.SocialAccountRepository;
import com.example.socialhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TwitterOAuthService {

    private final UserRepository userRepository;

    @Value("${twitter.client-id}")
    private String clientId;

    @Value("${twitter.client-secret}")
    private String clientSecret;

    @Value("${http://localhost:8080/api/accounts/callback/twitter}")
    private String callbackUrl;

    private final SocialAccountRepository accountRepository;

    public String getAuthorizationUrl(Long userId) {
        // Generate secure state parameter
        String state = Base64.getUrlEncoder()
                .encodeToString((userId + ":" + System.currentTimeMillis()).getBytes());

        return "https://twitter.com/i/oauth2/authorize?" +
                "response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8) +
                "&scope=tweet.read%20tweet.write%20users.read%20follows.read%20offline.access" +
                "&state=" + state +
                "&code_challenge=challenge" +
                "&code_challenge_method=plain";
    }
    private Long extractUserIdFromState(String state) {
        String decoded = new String(Base64.getUrlDecoder().decode(state));
        return Long.parseLong(decoded.split(":")[0]);
    }

    private LocalDateTime calculateExpiry(Long expiresIn) {
        return LocalDateTime.from(java.time.Instant.now().plusSeconds(expiresIn != null ? expiresIn : 0));
    }


    public SocialAccount handleCallback(String code, String state) {
        // Extract user ID from state
        Long userId = extractUserIdFromState(state);

        // Exchange code for access token
        TwitterAccessToken tokenResponse = exchangeCodeForToken(code);

        // Get user profile information
        TwitterUserProfile profile = getUserProfile(tokenResponse.getAccessToken());

        // Save social account
        SocialAccount account = new SocialAccount();
        account.setUser((User) userRepository);
        account.setPlatform(Platform.TWITTER);
        account.setAccountName(profile.getUsername());
        account.setAccountId(profile.getId());
        account.setAccessToken(tokenResponse.getAccessToken());
        account.setRefreshToken(tokenResponse.getRefreshToken());
        account.setTokenExpiry(calculateExpiry(tokenResponse.getExpiresIn()));
        account.setProfileImageUrl(profile.getProfileImageUrl());
        account.setFollowersCount(profile.getPublicMetrics().getFollowersCount());

        return accountRepository.save(account);
    }

    private TwitterAccessToken exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", callbackUrl);
        params.add("code_verifier", "challenge");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TwitterAccessToken> response = restTemplate.postForEntity(
                "https://api.twitter.com/2/oauth2/token",
                request,
                TwitterAccessToken.class
        );

        return response.getBody();
    }

    private TwitterUserProfile getUserProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<TwitterUserResponse> response = restTemplate.exchange(
                "https://api.twitter.com/2/users/me?user.fields=profile_image_url,public_metrics",
                HttpMethod.GET,
                request,
                TwitterUserResponse.class
        );

        return response.getBody().getData();
    }
//    User user = userRepository.findById(userId)
//            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

}
