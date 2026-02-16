package com.example.socialhive.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.entity.ScheduledPost;
import com.example.socialhive.entity.PostStatus;
import com.example.socialhive.repository.ScheduledPostRepository;
import com.example.socialhive.repository.SocialAccountRepository;
import com.example.socialhive.dto.CreatePostRequest;
import com.example.socialhive.dto.UpdatePostRequest;
import com.example.socialhive.exception.ResourceNotFoundException;
import com.example.socialhive.exception.UnauthorizedException;
import com.example.socialhive.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final ScheduledPostRepository postRepository;
    private final SocialAccountRepository accountRepository;

    public ScheduledPost createPost(CreatePostRequest request, Long userId) {

        // Validate account ownership
        SocialAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        log.info("UserId: {}", userId);
        log.info("Account userId: {}", account.getUser().getId());

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to use this account");
        }

        // Validate scheduled time is in future
        if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Scheduled time must be in the future");
        }

        // Validate content is not empty and within limits
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("Content cannot be empty");
        }

        // Validate content length (e.g., Twitter limit)
        if (request.getContent().length() > 280) {
            throw new RuntimeException("Content exceeds 280 characters");
        }

        // Create post
        ScheduledPost post = new ScheduledPost();
        post.setAccount(account);
        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls());
        post.setScheduledTime(request.getScheduledTime());
        post.setHashtags(request.getHashtags());
        post.setStatus(PostStatus.PENDING);

        return postRepository.save(post);
    }

    public ScheduledPost updatePost(Long postId, UpdatePostRequest request, Long userId) {
        ScheduledPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Verify ownership
        if (!post.getAccount().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized");
        }

        // Can only update pending posts
        if (post.getStatus() != PostStatus.PENDING) {
            throw new ValidationException("Can only update pending posts");
        }

        // Update fields
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getScheduledTime() != null) {
            if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Scheduled time must be in the future");
            }
            post.setScheduledTime(request.getScheduledTime());
        }
        if (request.getMediaUrls() != null) {
            post.setMediaUrls(request.getMediaUrls());
        }

        return postRepository.save(post);
    }

    public void cancelPost(Long postId, Long userId) {
        ScheduledPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAccount().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized");
        }

        if (post.getStatus() != PostStatus.PENDING) {
            throw new ValidationException("Can only cancel pending posts");
        }

        post.setStatus(PostStatus.CANCELLED);
        postRepository.save(post);
    }

    public List<ScheduledPost> getPostsByAccount(Long accountId, Long userId) {
        SocialAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized");
        }

        return postRepository.findByAccountIdOrderByScheduledTimeDesc(accountId);
    }

    public List<ScheduledPost> getAllUserPosts(Long userId) {
        return postRepository.findByAccountUserIdOrderByScheduledTimeDesc(userId);
    }

    public ScheduledPost getPost(Long postId, Long userId) {
        ScheduledPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAccount().getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        return post;
    }

    public List<ScheduledPost> getPostsByStatus(Long userId, PostStatus status) {
        return postRepository.findByAccountUserIdAndStatus(userId, status);
    }

}