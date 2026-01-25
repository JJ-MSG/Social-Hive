package com.example.socialhive.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "platform", "account_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER RELATION =================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // ================= PLATFORM =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    // ================= SOCIAL ACCOUNT INFO =================
    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "followers_count")
    private Integer followersCount;

    // ================= TOKENS =================
    @Column(name = "access_token", length = 2000)
    private String accessToken;

    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;

    @Column(name = "token_expiry")
    private Instant tokenExpiry;
}
