package com.example.socialhive.dto;

import lombok.Data;

@Data
public class TwitterUserProfile {
    private String id;
    private String username;
    private String profileImageUrl;
    private PublicMetrics publicMetrics;

    @Data
    public static class PublicMetrics {
        private int followersCount;
    }
}
