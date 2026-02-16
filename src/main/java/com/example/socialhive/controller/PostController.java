package com.example.socialhive.controller;

import com.example.socialhive.dto.CreatePostRequest;
import com.example.socialhive.dto.UpdatePostRequest;
import com.example.socialhive.dto.PostResponse;
import com.example.socialhive.entity.PostStatus;
import com.example.socialhive.entity.ScheduledPost;
import com.example.socialhive.entity.User;
import com.example.socialhive.repository.UserRepository;
import com.example.socialhive.service.PostService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        ScheduledPost post = postService.createPost(request, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(post));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<ScheduledPost> posts = postService.getAllUserPosts(user.getId());

        return ResponseEntity.ok(posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        ScheduledPost post = postService.getPost(postId, user.getId());

        return ResponseEntity.ok(toResponse(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId,
                                                   @Valid @RequestBody UpdatePostRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        ScheduledPost post = postService.updatePost(postId, request, user.getId());

        return ResponseEntity.ok(toResponse(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> cancelPost(@PathVariable Long postId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        postService.cancelPost(postId, user.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PostResponse>> getPostsByStatus(@PathVariable PostStatus status,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<ScheduledPost> posts = postService.getPostsByStatus(user.getId(), status);

        return ResponseEntity.ok(posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private PostResponse toResponse(ScheduledPost post) {
        return new PostResponse(
                post.getId(),
                post.getAccount().getId(),
                post.getContent(),
                post.getMediaUrls(),
                post.getScheduledTime(),
                post.getStatus(),
                post.getHashtags(),
                post.getCreatedAt()
        );
    }

}
