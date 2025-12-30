package com.example.socialhive.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstagramAccessToken {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("user_id")
    private String userId;
}
