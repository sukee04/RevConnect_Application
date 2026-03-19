package com.project.revconnect.repository;

import com.project.revconnect.model.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    List<FollowRequest> findByReceiver_IdAndStatusOrderByCreatedAtDesc(Long receiverId, String status);

    List<FollowRequest> findBySender_IdAndStatusOrderByCreatedAtDesc(Long senderId, String status);

    boolean existsBySender_IdAndReceiver_IdAndStatus(Long senderId, Long receiverId, String status);
}
