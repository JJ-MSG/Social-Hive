package com.example.socialhive.dto;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.enums.Platform;
import lombok.Data;

@Data
public class SocialAccountResponse {
    private Long id;
    private String platform;
    private String accountName;
    private String accountId;


}

