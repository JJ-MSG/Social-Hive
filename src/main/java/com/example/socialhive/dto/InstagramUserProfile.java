package com.example.socialhive.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstagramUserProfile {
    private String id;

    private String username;

    @JsonProperty("followers_count")
    private Integer followersCount;
}

