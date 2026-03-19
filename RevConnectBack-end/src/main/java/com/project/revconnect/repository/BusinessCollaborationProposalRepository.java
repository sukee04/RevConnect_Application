package com.project.revconnect.repository;

import com.project.revconnect.model.BusinessCollaborationProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessCollaborationProposalRepository extends JpaRepository<BusinessCollaborationProposal, Long> {
    List<BusinessCollaborationProposal> findByBusinessUser_IdOrderByCreatedAtDesc(Long businessUserId);
    List<BusinessCollaborationProposal> findByCreatorUser_IdOrderByCreatedAtDesc(Long creatorUserId);
    void deleteByBusinessUser_IdOrCreatorUser_Id(Long businessUserId, Long creatorUserId);
}
