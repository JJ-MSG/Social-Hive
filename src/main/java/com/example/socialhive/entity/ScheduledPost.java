package com.example.socialhive.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scheduled_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private SocialAccount account;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "media_urls", columnDefinition = "JSON")
    private String mediaUrlsJson;

    @Transient
    private List<String> mediaUrls = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PostStatus status = PostStatus.PENDING;

    @Column(length = 100)
    private String platformPostId;

    @Column(length = 500)
    private String hashtags;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @PostLoad
    private void convertJsonToList() {
        if (mediaUrlsJson != null && !mediaUrlsJson.isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mediaUrls = mapper.readValue(mediaUrlsJson, new TypeReference<>() {
                });
            } catch (Exception e) {
                mediaUrls = new ArrayList<>();
            }
        }
    }

    @PrePersist
    @PreUpdate
    private void convertListToJson() {
        if (mediaUrls != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mediaUrlsJson = mapper.writeValueAsString(mediaUrls);
            } catch (Exception e) {
                mediaUrlsJson = "[]";
            }
        }
    }
}