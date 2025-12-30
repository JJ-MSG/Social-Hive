package com.example.socialhive.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

public class UpdatePostRequest {

    @Size(max = 500, message = "Content cannot exceed 500 characters")
    private String content;

    private List<String> mediaUrls;

    private List<String> hashtags;

    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledTime;

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}

