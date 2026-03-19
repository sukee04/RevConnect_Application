package com.project.revconnect.repository;

import com.project.revconnect.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;



@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser_Id(Long id);
    List<Post> findByUser_IdOrderByIsPinnedDescCreatedAtDesc(Long id);
    List<Post> findByUser_IdAndPublishedTrueOrderByCreatedAtDesc(Long id);
    List<Post> findByCollaborator_IdAndCollabAcceptedTrueOrderByCreatedAtDesc(Long id);
    List<Post> findByCollaborator_IdAndCollabAcceptedFalseOrderByCreatedAtDesc(Long id);
    List<Post> findByUser_IdAndMediaTypeIgnoreCaseAndSeriesNameIsNotNullOrderBySeriesNameAscSeriesOrderAscCreatedAtDesc(Long id, String mediaType);
    List<Post> findByPublishedFalseAndScheduledAtLessThanEqual(LocalDateTime now);
    long countByUser_Id(Long id);
    long countByUser_IdAndIsPinnedTrue(Long id);

    @Modifying
    @Query("UPDATE Post p SET p.collaborator = null, p.collabAccepted = true WHERE p.collaborator.id = :userId")
    int clearCollaboratorByUserId(@Param("userId") Long userId);
}
