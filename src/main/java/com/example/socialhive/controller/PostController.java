package com.example.socialhive.controller;

import com.example.socialhive.dto.CreatePostRequest;
import com.example.socialhive.dto.PostResponse;
import com.example.socialhive.dto.UpdatePostRequest;
import com.example.socialhive.entity.*;
import com.example.socialhive.enums.PostStatus;
import com.example.socialhive.service.PostService;
import com.example.socialhive.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {


    private final PostService postService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        ScheduledPost post = postService.createPost(request, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(post));
    }



    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        List<ScheduledPost> posts = postService.getAllUserPosts(user.getId());

        return ResponseEntity.ok(posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        ScheduledPost post = postService.getPost(postId, user.getId());

        return ResponseEntity.ok(toResponse(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId,
                                                   @Valid @RequestBody UpdatePostRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        ScheduledPost post = postService.updatePost(postId, request, user.getId());

        return ResponseEntity.ok(toResponse(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> cancelPost(@PathVariable Long postId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        postService.cancelPost(postId, user.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PostResponse>> getPostsByStatus(@PathVariable PostStatus status,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserFromDetails(userDetails);
        List<ScheduledPost> posts = postService.getPostsByStatus(user.getId(), status);

        return ResponseEntity.ok(posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }
    // In PostController.java
    private PostResponse toResponse(ScheduledPost post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .scheduledTime(post.getScheduledTime())
                .status(post.getStatus())
                .platform(post.getPlatform())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }



}


