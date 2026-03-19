package com.project.revconnect.repository;

import com.project.revconnect.model.Like;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<Like> findByPostOrderByIdDesc(Post post);
    void deleteByPost_Id(Long postId);
    void deleteByPost_User_Id(Long userId);
}
