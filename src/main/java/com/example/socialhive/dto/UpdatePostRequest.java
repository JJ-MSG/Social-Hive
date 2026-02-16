package com.example.socialhive.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdatePostRequest {
    private String content;
    private List<String> mediaUrls;
    private LocalDateTime scheduledTime;
    private String hashtags;
}
