package com.project.revconnect.repository;

import com.project.revconnect.model.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    List<SavedPost> findByUser_IdOrderByCreatedAtDesc(Long userId);

    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);

    void deleteByUser_IdAndPost_Id(Long userId, Long postId);
    void deleteByPost_Id(Long postId);
    void deleteByPost_User_Id(Long userId);

    long countByPost_Id(Long postId);
}
