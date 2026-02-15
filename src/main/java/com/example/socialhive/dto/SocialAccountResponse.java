package com.example.socialhive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialAccountResponse {
    private Long id;
    private String platform;
    private String accountName;
    private String accountId;
    private String profileImageUrl;
    private Integer followersCount;
}
