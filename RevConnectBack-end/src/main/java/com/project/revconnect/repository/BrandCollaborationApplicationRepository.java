package com.project.revconnect.repository;

import com.project.revconnect.model.BrandCollaborationApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandCollaborationApplicationRepository extends JpaRepository<BrandCollaborationApplication, Long> {
    Optional<BrandCollaborationApplication> findByOpportunity_IdAndCreator_Id(Long opportunityId, Long creatorId);
    List<BrandCollaborationApplication> findByCreator_IdOrderByCreatedAtDesc(Long creatorId);
    List<BrandCollaborationApplication> findByOpportunity_BusinessUser_IdOrderByCreatedAtDesc(Long businessUserId);
    List<BrandCollaborationApplication> findByOpportunity_IdOrderByCreatedAtDesc(Long opportunityId);
    void deleteByCreator_Id(Long creatorId);
    void deleteByOpportunity_BusinessUser_Id(Long businessUserId);
}
