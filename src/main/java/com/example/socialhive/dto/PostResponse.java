package com.example.socialhive.dto;

import com.example.socialhive.entity.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private Long accountId;
    private String content;
    private List<String> mediaUrls;
    private LocalDateTime scheduledTime;
    private PostStatus status;
    private String hashtags;
    private LocalDateTime createdAt;
}
