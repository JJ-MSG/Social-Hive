package com.example.socialhive.dto;



import lombok.Data;

@Data
public class TwitterAccessToken {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}

