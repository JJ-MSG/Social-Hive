package com.example.socialhive.service;
import com.example.socialhive.dto.InstagramAccessToken;
import com.example.socialhive.dto.InstagramLongLivedToken;
import com.example.socialhive.dto.InstagramUserProfile;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.entity.User;
import com.example.socialhive.enums.Platform;
import com.example.socialhive.repository.SocialAccountRepository;
import com.example.socialhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class InstagramOAuthService {

    private final UserRepository user;
    @Value("2129253507609114")
    private String appId;

    @Value("f88cd060e37f504aba1a144cd73f3d23")
    private String appSecret;

    @Value("${instagram.redirect-uri}")
    private String redirectUri;

    private final SocialAccountRepository accountRepository;

    public String getAuthorizationUrl(Long userId) {
        String state = Base64.getUrlEncoder()
                .encodeToString((userId + ":" + System.currentTimeMillis()).getBytes());

        return "https://www.facebook.com/dialog/oauth?" +
                "client_id=" + appId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=business_management,instagram_manage_comments,pages_show_list" +
                "&response_type=code" +
                "&state=" + state;
    }

    public SocialAccount handleCallback(String code, String state) {
        Long userId = extractUserIdFromState(state);

        // Exchange code for short-lived token
        InstagramAccessToken shortToken = exchangeCodeForToken(code);

        // Exchange for long-lived token
        InstagramLongLivedToken longToken = getLongLivedToken(shortToken.getAccessToken());

        // Get user profile
        InstagramUserProfile profile = getUserProfile(longToken.getAccessToken());

        // Save social account
        SocialAccount account = new SocialAccount();
        account.setUser((User) user);
        account.setPlatform(Platform.INSTAGRAM);
        account.setAccountName(profile.getUsername());
        account.setAccountId(profile.getId());
        account.setAccessToken(longToken.getAccessToken());
        account.setTokenExpiry(calculateExpiry(longToken.getExpiresIn()));
        account.setFollowersCount(profile.getFollowersCount());

        return accountRepository.save(account);
    }
    private Long extractUserIdFromState(String state) {
        String decoded = new String(Base64.getUrlDecoder().decode(state));
        return Long.parseLong(decoded.split(":")[0]);
    }

    private InstagramUserProfile getUserProfile(String accessToken) {
        String url = "https://graph.instagram.com/me?fields=id,username,followers_count&access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, InstagramUserProfile.class);
    }

    private LocalDateTime calculateExpiry(Long expiresIn) {
        return LocalDateTime.from(java.time.Instant.now().plusSeconds(expiresIn != null ? expiresIn : 0));
    }



    private InstagramAccessToken exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", appId);
        params.add("client_secret", appSecret);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        return restTemplate.postForObject(
                "https://graph.facebook.com/v20.0/oauth/access_token",
                params,
                InstagramAccessToken.class
        );
    }

    private InstagramLongLivedToken getLongLivedToken(String shortLivedToken) {
        String url = "https://graph.facebook.com/v20.0/oauth/access_token?" +
                "grant_type=fb_exchange_token" + "&client_id="+appId+
                "&client_secret=" + appSecret +
                "&fb_exchange_token=" + shortLivedToken;

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, InstagramLongLivedToken.class);
    }
}
