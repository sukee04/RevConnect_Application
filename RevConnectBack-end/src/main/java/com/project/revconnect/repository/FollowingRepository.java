package com.project.revconnect.repository;

import com.project.revconnect.model.Following;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FollowingRepository extends JpaRepository<Following, Long> {

    List<Following> findByUser(User user);

    Following findByUserAndFollowingUser(User user, User followingUser);

    List<Following> findByFollowingUser(User user);

    long countByUser(User user);

    long countByFollowingUser(User user);

    List<Following> findByFollowingUserAndCreatedAtAfterOrderByCreatedAtAsc(User followingUser, LocalDateTime createdAt);
}
