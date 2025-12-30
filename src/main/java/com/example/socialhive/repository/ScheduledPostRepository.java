package com.example.socialhive.repository;

import java.util.List;

import com.example.socialhive.entity.ScheduledPost;
import com.example.socialhive.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledPostRepository extends JpaRepository<ScheduledPost, Long> {
    List<ScheduledPost> findAllByAccount_User_IdOrderByScheduledTimeDesc(Long userId);
    List<ScheduledPost> findAllByAccount_User_IdAndStatusOrderByScheduledTimeDesc(Long userId, PostStatus status);
    List<ScheduledPost> findByAccountIdOrderByScheduledTimeDesc(Long accountId);

}

