package com.example.socialhive.repository;

import com.example.socialhive.entity.ScheduledPost;
import com.example.socialhive.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledPostRepository extends JpaRepository<ScheduledPost, Long> {
    List<ScheduledPost> findByAccountIdOrderByScheduledTimeDesc(Long accountId);
    List<ScheduledPost> findByAccountUserIdOrderByScheduledTimeDesc(Long userId); // convenient
    List<ScheduledPost> findByAccountUserIdAndStatusOrderByScheduledTimeDesc(Long userId, PostStatus status);
    List<ScheduledPost> findByAccountUserIdAndStatus(Long userId, PostStatus status);

}
