package com.example.socialhive.util;



import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PkceUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PkceUtils() { }

    public static String randomState() {
        return generateRandomBase64Url(32);
    }

    public static String generateCodeVerifier() {
        // RFC 7636 recommends code verifier length between 43 and 128 characters
        return generateRandomBase64Url(64);
    }

    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String generateRandomBase64Url(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

