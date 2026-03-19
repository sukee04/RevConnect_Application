package com.project.revconnect.repository;

import com.project.revconnect.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {


    List<Story> findByUser_IdAndActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);


    @Query("SELECT s FROM Story s WHERE " +
            "(s.user.id = :userId OR s.user.id IN " +
            "(SELECT f.followingUser.id FROM Following f WHERE f.user.id = :userId) OR " +
            "s.user.id IN (SELECT cs.creator.id FROM CreatorSubscription cs WHERE cs.subscriber.id = :userId AND cs.status = 'ACTIVE')) " +
            "AND s.active = true AND s.expiresAt > :now " +
            "ORDER BY s.createdAt DESC")
    List<Story> findFeedStories(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

