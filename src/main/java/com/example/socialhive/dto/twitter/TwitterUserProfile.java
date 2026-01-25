package com.example.socialhive.dto.twitter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TwitterUserProfile {

    private String id;
    private String username;
    private String name;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("public_metrics")
    private PublicMetrics publicMetrics;

    @Data
    public static class PublicMetrics {
        @JsonProperty("followers_count")
        private Integer followersCount;
    }
}
