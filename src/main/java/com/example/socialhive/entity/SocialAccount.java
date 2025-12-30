package com.example.socialhive.entity;

import com.example.socialhive.enums.Platform;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts")
@Data
@NoArgsConstructor
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Relationship to User (ONLY this, no userId field)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform; // INSTAGRAM, TWITTER

    @Column(nullable = false)
    private String accountName; // username

    @Column(nullable = false)
    private String accountId; // platform user ID

    @Column(length = 1000)
    private String accessToken;

    private String refreshToken;

    private LocalDateTime tokenExpiry;

    private Integer followersCount;

    private String profileImageUrl;
}
