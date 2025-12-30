package com.example.socialhive.service;
import com.example.socialhive.dto.CreatePostRequest;
import com.example.socialhive.dto.UpdatePostRequest;
import com.example.socialhive.entity.ScheduledPost;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.enums.PostStatus;
import com.example.socialhive.repository.ScheduledPostRepository;
import com.example.socialhive.repository.SocialAccountRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.socialhive.exception.ResourceNotFoundException;
import com.example.socialhive.exception.UnauthorizedException;
import com.example.socialhive.exception.ValidationException;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final ScheduledPostRepository postRepository;
    private final SocialAccountRepository accountRepository;


    public ScheduledPost createPost(CreatePostRequest request, Long userId) {
        // Validate account ownership
        SocialAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to use this account");
        }

        // Validate scheduled time is in future
        if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Scheduled time must be in the future");
        }

        // Create post
        ScheduledPost post = new ScheduledPost();
        post.setAccount(account);
        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls());
        post.setScheduledTime(request.getScheduledTime());
        post.setHashtags(String.valueOf(request.getHashtags()));
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
// Add these methods to PostService

    public List<ScheduledPost> getAllUserPosts(Long userId) {
        // Fetch all posts for the user
        return postRepository.findAllByAccount_User_IdOrderByScheduledTimeDesc(userId);
    }

    public ScheduledPost getPost(Long postId, Long userId) {
        ScheduledPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getAccount().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized");
        }
        return post;
    }

    public List<ScheduledPost> getPostsByStatus(Long userId, PostStatus status) {
        return postRepository.findAllByAccount_User_IdAndStatusOrderByScheduledTimeDesc(userId, status);
    }

}
