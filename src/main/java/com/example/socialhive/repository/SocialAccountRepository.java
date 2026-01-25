package com.example.socialhive.repository;

import com.example.socialhive.entity.Platform;
import com.example.socialhive.entity.SocialAccount;
import com.example.socialhive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository
        extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByUserAndPlatform(
            User user,
            Platform platform
    );

    List<SocialAccount> findAllByUser(User user);

    boolean existsByUserAndPlatformAndAccountId(
            User user,
            Platform platform,
            String accountId
    );
}
