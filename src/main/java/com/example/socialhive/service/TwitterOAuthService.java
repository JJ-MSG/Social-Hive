package com.example.socialhive.service;

import com.example.socialhive.dto.twitter.TwitterAccessToken;
import com.example.socialhive.dto.twitter.TwitterUserProfile;
import com.example.socialhive.dto.twitter.TwitterUserResponse;
import com.example.socialhive.entity.Platform;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.entity.User;
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
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TwitterOAuthService {

    @Value("${twitter.client-id}")
    private String clientId;

    @Value("${twitter.client-secret}")
    private String clientSecret;

    @Value("${twitter.callback-url}")
    private String callbackUrl;

    private final SocialAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // ================= AUTHORIZE =================
    public String getAuthorizationUrl(Long userId) {

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

    // ================= CALLBACK =================
    public SocialAccount handleCallback(String code, String state) {

        Long userId = extractUserIdFromState(state);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        TwitterAccessToken tokenResponse = exchangeCodeForToken(code);
        TwitterUserProfile profile = getUserProfile(tokenResponse.getAccessToken());

        SocialAccount account = SocialAccount.builder()
                .user(user)
                .platform(Platform.TWITTER)
                .accountId(profile.getId())
                .accountName(profile.getUsername())
                .profileImageUrl(profile.getProfileImageUrl())
                .followersCount(profile.getPublicMetrics().getFollowersCount())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenExpiry(calculateExpiry(tokenResponse.getExpiresIn()))
                .build();

        return accountRepository.save(account);
    }

    // ================= TOKEN EXCHANGE =================
    private TwitterAccessToken exchangeCodeForToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", callbackUrl);
        params.add("code_verifier", "challenge");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<TwitterAccessToken> response =
                restTemplate.postForEntity(
                        "https://api.twitter.com/2/oauth2/token",
                        request,
                        TwitterAccessToken.class
                );

        return response.getBody();
    }

    // ================= USER PROFILE =================
    private TwitterUserProfile getUserProfile(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<TwitterUserResponse> response =
                restTemplate.exchange(
                        "https://api.twitter.com/2/users/me?user.fields=profile_image_url,public_metrics",
                        HttpMethod.GET,
                        request,
                        TwitterUserResponse.class
                );

        return response.getBody().getData();
    }

    // ================= HELPERS =================
    private Long extractUserIdFromState(String state) {
        String decoded = new String(Base64.getUrlDecoder().decode(state));
        return Long.parseLong(decoded.split(":")[0]);
    }

    private Instant calculateExpiry(Long expiresIn) {
        return Instant.now().plusSeconds(expiresIn);
    }
}
