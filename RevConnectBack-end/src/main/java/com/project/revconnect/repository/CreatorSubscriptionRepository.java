package com.project.revconnect.repository;

import com.project.revconnect.model.CreatorSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreatorSubscriptionRepository extends JpaRepository<CreatorSubscription, Long> {

    Optional<CreatorSubscription> findByCreator_IdAndSubscriber_Id(Long creatorId, Long subscriberId);

    boolean existsByCreator_IdAndSubscriber_IdAndStatus(Long creatorId, Long subscriberId, String status);

    List<CreatorSubscription> findBySubscriber_IdAndStatus(Long subscriberId, String status);
    void deleteByCreator_IdOrSubscriber_Id(Long creatorId, Long subscriberId);
}
