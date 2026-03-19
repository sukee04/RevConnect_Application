package com.project.revconnect.repository;

import com.project.revconnect.model.PostViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostViewEventRepository extends JpaRepository<PostViewEvent, Long> {
    long countByPost_Id(Long postId);
    void deleteByPost_Id(Long postId);
    void deleteByViewer_Id(Long viewerId);
    void deleteByPost_User_Id(Long userId);

    long countByPost_IdAndCompletedTrue(Long postId);

    @Query("SELECT COALESCE(SUM(p.watchSeconds), 0) FROM PostViewEvent p WHERE p.post.id = :postId")
    double sumWatchSecondsByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(DISTINCT p.viewer.id) FROM PostViewEvent p WHERE p.post.id = :postId")
    long countDistinctViewersByPostId(@Param("postId") Long postId);
}
