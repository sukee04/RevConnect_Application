package com.project.revconnect.repository;

import com.project.revconnect.model.Post;
import com.project.revconnect.model.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PostTag pt WHERE pt.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostTag pt WHERE pt.post.user.id = :userId")
    void deleteByPostUserId(@Param("userId") Long userId);

    @Query("SELECT pt.post FROM PostTag pt WHERE LOWER(pt.taggedUser.username) = LOWER(:username) ORDER BY pt.createdAt DESC")
    List<Post> findTaggedPostsByUsername(@Param("username") String username);
}
