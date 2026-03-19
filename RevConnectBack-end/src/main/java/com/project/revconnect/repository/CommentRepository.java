package com.project.revconnect.repository;

import com.project.revconnect.model.Comment;
import com.project.revconnect.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
    long countByPost(Post post);
    void deleteByPost_Id(Long postId);
    void deleteByPost_User_Id(Long userId);
}
