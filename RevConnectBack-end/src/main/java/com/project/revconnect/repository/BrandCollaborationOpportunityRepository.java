package com.project.revconnect.repository;

import com.project.revconnect.model.BrandCollaborationOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandCollaborationOpportunityRepository extends JpaRepository<BrandCollaborationOpportunity, Long> {
    List<BrandCollaborationOpportunity> findByStatusOrderByCreatedAtDesc(String status);
    List<BrandCollaborationOpportunity> findByStatusAndCreatorCategoryIgnoreCaseOrderByCreatedAtDesc(String status, String creatorCategory);
    List<BrandCollaborationOpportunity> findByBusinessUser_IdOrderByCreatedAtDesc(Long businessUserId);
    void deleteByBusinessUser_Id(Long businessUserId);
}
