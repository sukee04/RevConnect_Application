package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.*;
import com.project.revconnect.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandCollaborationService {

    private final BrandCollaborationOpportunityRepository opportunityRepository;
    private final BrandCollaborationApplicationRepository applicationRepository;
    private final BusinessCollaborationProposalRepository proposalRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BrandCollaborationService(BrandCollaborationOpportunityRepository opportunityRepository,
                                     BrandCollaborationApplicationRepository applicationRepository,
                                     BusinessCollaborationProposalRepository proposalRepository,
                                     PostRepository postRepository,
                                     PostTagRepository postTagRepository,
                                     UserRepository userRepository,
                                     NotificationService notificationService) {
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.proposalRepository = proposalRepository;
        this.postRepository = postRepository;
        this.postTagRepository = postTagRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getLoggedInUser() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }
        return user;
    }

    public BrandCollaborationOpportunity createOpportunity(BrandCollaborationOpportunity request) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can create collaboration opportunities");
        }

        BrandCollaborationOpportunity opportunity = new BrandCollaborationOpportunity();
        opportunity.setBusinessUser(business);
        opportunity.setTitle(trimToLength(request.getTitle(), 180));
        opportunity.setDescription(trimToLength(request.getDescription(), 2000));
        opportunity.setCreatorCategory(trimToLength(request.getCreatorCategory(), 120));
        opportunity.setMinBudget(request.getMinBudget());
        opportunity.setMaxBudget(request.getMaxBudget());
        opportunity.setStatus("OPEN");

        if (opportunity.getTitle() == null || opportunity.getTitle().isBlank()) {
            throw new RuntimeException("Title is required");
        }
        if (opportunity.getDescription() == null || opportunity.getDescription().isBlank()) {
            throw new RuntimeException("Description is required");
        }

        BrandCollaborationOpportunity savedOpportunity = opportunityRepository.save(opportunity);
        notifyCreatorsForOpenOpportunity(savedOpportunity, business);
        return savedOpportunity;
    }

    public List<BrandCollaborationOpportunity> listOpenOpportunities(String creatorCategory) {
        User viewer = getLoggedInUser();
        if (creatorCategory == null || creatorCategory.isBlank()) {
            List<BrandCollaborationOpportunity> opportunities = opportunityRepository.findByStatusOrderByCreatedAtDesc("OPEN");
            maybeIncrementOpportunityViews(viewer, opportunities);
            return opportunities;
        }
        List<BrandCollaborationOpportunity> opportunities = opportunityRepository.findByStatusAndCreatorCategoryIgnoreCaseOrderByCreatedAtDesc(
                "OPEN",
                creatorCategory.trim());
        maybeIncrementOpportunityViews(viewer, opportunities);
        return opportunities;
    }

    public List<BrandCollaborationOpportunity> getMyBusinessOpportunities() {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can access this endpoint");
        }
        return opportunityRepository.findByBusinessUser_IdOrderByCreatedAtDesc(business.getId());
    }

    public Map<String, Object> applyToOpportunity(Long opportunityId, Map<String, String> payload) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can apply");
        }

        BrandCollaborationOpportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));

        if (!"OPEN".equalsIgnoreCase(opportunity.getStatus())) {
            throw new RuntimeException("This opportunity is not open for applications");
        }

        applicationRepository.findByOpportunity_IdAndCreator_Id(opportunityId, creator.getId())
                .ifPresent(existing -> {
                    throw new RuntimeException("You have already applied to this opportunity");
                });

        String pitchMessage = payload == null ? null : payload.get("pitchMessage");
        if (pitchMessage == null || pitchMessage.isBlank()) {
            throw new RuntimeException("Pitch message is required");
        }

        BrandCollaborationApplication application = new BrandCollaborationApplication();
        application.setOpportunity(opportunity);
        application.setCreator(creator);
        application.setPitchMessage(trimToLength(pitchMessage, 1500));
        application.setStatus("PENDING");
        applicationRepository.save(application);

        return Map.of("message", "Application submitted");
    }

    public List<BrandCollaborationApplication> getMyApplications() {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can access this endpoint");
        }
        return applicationRepository.findByCreator_IdOrderByCreatedAtDesc(creator.getId());
    }

    public List<BrandCollaborationApplication> getApplicationsForOpportunity(Long opportunityId) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can access this endpoint");
        }

        BrandCollaborationOpportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));

        if (!opportunity.getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return applicationRepository.findByOpportunity_IdOrderByCreatedAtDesc(opportunityId);
    }

    public Map<String, Object> updateApplicationStatus(Long applicationId, Map<String, String> payload) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can update application status");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getOpportunity().getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String status = payload == null ? null : payload.get("status");
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("PENDING") && !normalized.equals("ACCEPTED") && !normalized.equals("REJECTED")) {
            throw new RuntimeException("Status must be one of PENDING, ACCEPTED, REJECTED");
        }

        application.setStatus(normalized);
        applicationRepository.save(application);
        if ("ACCEPTED".equals(normalized)) {
            notificationService.createNotification(application.getCreator(), business, "COLLAB_APPLICATION_ACCEPTED", application.getId());
        } else if ("REJECTED".equals(normalized)) {
            notificationService.createNotification(application.getCreator(), business, "COLLAB_APPLICATION_REJECTED", application.getId());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Application status updated");
        response.put("status", normalized);
        return response;
    }

    public Map<String, Object> startApplicationPromotion(Long applicationId) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can start promotion");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getOpportunity().getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"ACCEPTED".equals(current) && !"PROMOTION_REQUESTED".equals(current)) {
            throw new RuntimeException("Only accepted applications can be moved to promotion");
        }

        application.setStatus("PROMOTION_REQUESTED");
        if (application.getPromotionRequestedAt() == null) {
            application.setPromotionRequestedAt(LocalDateTime.now());
        }
        applicationRepository.save(application);
        return Map.of("message", "Promotion request sent to creator", "status", "PROMOTION_REQUESTED");
    }

    public Map<String, Object> completeApplicationPromotion(Long applicationId) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can complete promotion");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getOpportunity().getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"CREATOR_CONFIRMED".equals(current)) {
            throw new RuntimeException("Creator must confirm completion before business can complete");
        }

        application.setStatus("COMPLETED");
        application.setCompletedAt(LocalDateTime.now());
        applicationRepository.save(application);
        return Map.of("message", "Application promotion completed", "status", "COMPLETED");
    }

    public Map<String, Object> requestApplicationPromotion(Long applicationId, Map<String, String> payload) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can send promotion details");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getOpportunity().getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"ACCEPTED".equals(current)) {
            throw new RuntimeException("Promotion details can only be sent for accepted applications");
        }

        String details = trimToLength(payload == null ? null : payload.get("promotionDetails"), 2000);
        if (details == null || details.isBlank()) {
            throw new RuntimeException("Promotion details are required");
        }
        String productImageUrl = trimToLength(payload == null ? null : payload.get("promotionProductImageUrl"), 2_000_000);
        String productLink = trimToLength(payload == null ? null : payload.get("promotionProductLink"), 500);
        Long businessPostId = parseLong(payload == null ? null : payload.get("promotionBusinessPostId"));

        application.setPromotionDetails(details);
        application.setPromotionProductImageUrl(productImageUrl);
        application.setPromotionProductLink(productLink);
        application.setPromotionBusinessPostId(businessPostId);
        application.setStatus("PROMOTION_REQUESTED");
        application.setPromotionRequestedAt(LocalDateTime.now());
        applicationRepository.save(application);
        notificationService.createNotification(application.getCreator(), business, "COLLAB_PROMOTION_REQUESTED", application.getId());
        return Map.of("message", "Promotion request sent to creator", "status", "PROMOTION_REQUESTED");
    }

    public Map<String, Object> creatorAcceptApplicationPromotion(Long applicationId) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can accept promotion requests");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"PROMOTION_REQUESTED".equals(current)) {
            throw new RuntimeException("Only requested promotions can be accepted");
        }

        application.setStatus("IN_PROMOTION");
        application.setPromotionAcceptedAt(LocalDateTime.now());
        applicationRepository.save(application);
        notificationService.createNotification(application.getOpportunity().getBusinessUser(), creator, "COLLAB_PROMOTION_ACCEPTED", application.getId());
        return Map.of("message", "Promotion accepted by creator", "status", "IN_PROMOTION");
    }

    public Map<String, Object> creatorConfirmApplicationPromotion(Long applicationId, Map<String, String> payload) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can confirm promotion completion");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"IN_PROMOTION".equals(current)) {
            throw new RuntimeException("Only in-promotion collaborations can be confirmed");
        }

        String note = trimToLength(payload == null ? null : payload.get("confirmationNote"), 1500);
        if (note == null || note.isBlank()) {
            throw new RuntimeException("Confirmation note is required");
        }

        application.setCreatorConfirmationNote(note);
        application.setStatus("CREATOR_CONFIRMED");
        application.setCreatorConfirmedAt(LocalDateTime.now());
        applicationRepository.save(application);
        notificationService.createNotification(application.getOpportunity().getBusinessUser(), creator, "COLLAB_PROMOTION_CONFIRMED", application.getId());
        return Map.of("message", "Creator confirmation sent to business", "status", "CREATOR_CONFIRMED");
    }

    public Map<String, Object> completeApplicationPromotionAndPay(Long applicationId, Map<String, Object> payload) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can complete and pay");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getOpportunity().getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"CREATOR_CONFIRMED".equals(current)) {
            throw new RuntimeException("Creator confirmation is required before completion and payment");
        }

        Double amount = parseDouble(payload == null ? null : payload.get("paymentAmount"));
        String reference = trimToLength(parseString(payload == null ? null : payload.get("paymentReference")), 120);
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Valid payment amount is required");
        }

        LocalDateTime now = LocalDateTime.now();
        application.setStatus("COMPLETED");
        application.setCompletedAt(now);
        application.setPaymentAmount(amount);
        application.setPaymentReference(reference);
        application.setPaymentStatus("PAID");
        application.setPaidAt(now);
        applicationRepository.save(application);
        notificationService.createNotification(application.getCreator(), business, "COLLAB_PAYMENT_DONE", application.getId());

        return Map.of("message", "Promotion completed and payment marked", "status", "COMPLETED", "paymentStatus", "PAID");
    }

    public Map<String, Object> closeOpportunity(Long opportunityId) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can close opportunities");
        }

        BrandCollaborationOpportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));

        if (!opportunity.getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        opportunity.setStatus("CLOSED");
        opportunityRepository.save(opportunity);
        return Map.of("message", "Opportunity closed");
    }

    public BusinessCollaborationProposal sendDirectProposal(Map<String, Object> payload) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can send direct proposals");
        }

        Long creatorId = parseLong(payload == null ? null : payload.get("creatorId"));
        String creatorUsername = parseString(payload == null ? null : payload.get("creatorUsername"));

        User creator = null;
        if (creatorId != null && creatorId > 0) {
            creator = userRepository.findById(creatorId)
                    .orElseThrow(() -> new RuntimeException("Creator not found"));
        } else if (creatorUsername != null && !creatorUsername.isBlank()) {
            creator = userRepository.findByUsername(creatorUsername.trim());
            if (creator == null) {
                throw new RuntimeException("Creator not found");
            }
        }

        if (creator == null || creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Target user must be a creator");
        }
        if (creator.getId().equals(business.getId())) {
            throw new RuntimeException("Cannot send proposal to yourself");
        }

        String title = trimToLength(parseString(payload == null ? null : payload.get("title")), 180);
        String message = trimToLength(parseString(payload == null ? null : payload.get("message")), 2000);
        Double budget = parseDouble(payload == null ? null : payload.get("budget"));

        if (title == null || title.isBlank()) {
            throw new RuntimeException("Proposal title is required");
        }
        if (message == null || message.isBlank()) {
            throw new RuntimeException("Proposal message is required");
        }

        BusinessCollaborationProposal proposal = new BusinessCollaborationProposal();
        proposal.setBusinessUser(business);
        proposal.setCreatorUser(creator);
        proposal.setTitle(title);
        proposal.setMessage(message);
        proposal.setBudget(budget);
        proposal.setStatus("PENDING");
        BusinessCollaborationProposal savedProposal = proposalRepository.save(proposal);
        notificationService.createNotification(creator, business, "COLLAB_DIRECT_PROPOSAL_SENT", savedProposal.getId());
        return savedProposal;
    }

    public List<BusinessCollaborationProposal> getMySentDirectProposals() {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can access this endpoint");
        }
        return proposalRepository.findByBusinessUser_IdOrderByCreatedAtDesc(business.getId());
    }

    public List<BusinessCollaborationProposal> getMyReceivedDirectProposals() {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can access this endpoint");
        }
        return proposalRepository.findByCreatorUser_IdOrderByCreatedAtDesc(creator.getId());
    }

    public Map<String, Object> updateDirectProposalStatus(Long proposalId, Map<String, String> payload) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can update proposal status");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getCreatorUser().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String status = payload == null ? null : payload.get("status");
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("PENDING") && !normalized.equals("ACCEPTED") && !normalized.equals("REJECTED")) {
            throw new RuntimeException("Status must be one of PENDING, ACCEPTED, REJECTED");
        }

        proposal.setStatus(normalized);
        proposalRepository.save(proposal);
        if ("ACCEPTED".equals(normalized)) {
            notificationService.createNotification(proposal.getBusinessUser(), creator, "COLLAB_DIRECT_PROPOSAL_ACCEPTED", proposal.getId());
        } else if ("REJECTED".equals(normalized)) {
            notificationService.createNotification(proposal.getBusinessUser(), creator, "COLLAB_DIRECT_PROPOSAL_REJECTED", proposal.getId());
        }

        return Map.of("message", "Direct proposal status updated", "status", normalized);
    }

    public Map<String, Object> startDirectProposalPromotion(Long proposalId) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can start proposal promotion");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"ACCEPTED".equals(current) && !"PROMOTION_REQUESTED".equals(current)) {
            throw new RuntimeException("Only accepted direct proposals can be moved to promotion");
        }

        proposal.setStatus("PROMOTION_REQUESTED");
        if (proposal.getPromotionRequestedAt() == null) {
            proposal.setPromotionRequestedAt(LocalDateTime.now());
        }
        proposalRepository.save(proposal);
        return Map.of("message", "Promotion request sent to creator", "status", "PROMOTION_REQUESTED");
    }

    public Map<String, Object> completeDirectProposalPromotion(Long proposalId) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can complete proposal promotion");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"CREATOR_CONFIRMED".equals(current)) {
            throw new RuntimeException("Creator must confirm completion before business can complete");
        }

        proposal.setStatus("COMPLETED");
        proposal.setCompletedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        return Map.of("message", "Direct proposal promotion completed", "status", "COMPLETED");
    }

    public Map<String, Object> requestDirectProposalPromotion(Long proposalId, Map<String, String> payload) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can send promotion details");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"ACCEPTED".equals(current)) {
            throw new RuntimeException("Promotion details can only be sent for accepted proposals");
        }

        String details = trimToLength(payload == null ? null : payload.get("promotionDetails"), 2000);
        if (details == null || details.isBlank()) {
            throw new RuntimeException("Promotion details are required");
        }
        String productImageUrl = trimToLength(payload == null ? null : payload.get("promotionProductImageUrl"), 2_000_000);
        String productLink = trimToLength(payload == null ? null : payload.get("promotionProductLink"), 500);
        Long businessPostId = parseLong(payload == null ? null : payload.get("promotionBusinessPostId"));

        proposal.setPromotionDetails(details);
        proposal.setPromotionProductImageUrl(productImageUrl);
        proposal.setPromotionProductLink(productLink);
        proposal.setPromotionBusinessPostId(businessPostId);
        proposal.setStatus("PROMOTION_REQUESTED");
        proposal.setPromotionRequestedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        notificationService.createNotification(proposal.getCreatorUser(), business, "COLLAB_PROMOTION_REQUESTED", proposal.getId());
        return Map.of("message", "Promotion request sent to creator", "status", "PROMOTION_REQUESTED");
    }

    public Map<String, Object> creatorAcceptDirectProposalPromotion(Long proposalId) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can accept promotion requests");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getCreatorUser().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"PROMOTION_REQUESTED".equals(current)) {
            throw new RuntimeException("Only requested promotions can be accepted");
        }

        proposal.setStatus("IN_PROMOTION");
        proposal.setPromotionAcceptedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        notificationService.createNotification(proposal.getBusinessUser(), creator, "COLLAB_PROMOTION_ACCEPTED", proposal.getId());
        return Map.of("message", "Promotion accepted by creator", "status", "IN_PROMOTION");
    }

    public Map<String, Object> creatorConfirmDirectProposalPromotion(Long proposalId, Map<String, String> payload) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can confirm promotion completion");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getCreatorUser().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"IN_PROMOTION".equals(current)) {
            throw new RuntimeException("Only in-promotion collaborations can be confirmed");
        }

        String note = trimToLength(payload == null ? null : payload.get("confirmationNote"), 1500);
        if (note == null || note.isBlank()) {
            throw new RuntimeException("Confirmation note is required");
        }

        proposal.setCreatorConfirmationNote(note);
        proposal.setStatus("CREATOR_CONFIRMED");
        proposal.setCreatorConfirmedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        notificationService.createNotification(proposal.getBusinessUser(), creator, "COLLAB_PROMOTION_CONFIRMED", proposal.getId());
        return Map.of("message", "Creator confirmation sent to business", "status", "CREATOR_CONFIRMED");
    }

    public Map<String, Object> completeDirectProposalPromotionAndPay(Long proposalId, Map<String, Object> payload) {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can complete and pay");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (!proposal.getBusinessUser().getId().equals(business.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"CREATOR_CONFIRMED".equals(current)) {
            throw new RuntimeException("Creator confirmation is required before completion and payment");
        }

        Double amount = parseDouble(payload == null ? null : payload.get("paymentAmount"));
        String reference = trimToLength(parseString(payload == null ? null : payload.get("paymentReference")), 120);
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Valid payment amount is required");
        }

        LocalDateTime now = LocalDateTime.now();
        proposal.setStatus("COMPLETED");
        proposal.setCompletedAt(now);
        proposal.setPaymentAmount(amount);
        proposal.setPaymentReference(reference);
        proposal.setPaymentStatus("PAID");
        proposal.setPaidAt(now);
        proposalRepository.save(proposal);
        notificationService.createNotification(proposal.getCreatorUser(), business, "COLLAB_PAYMENT_DONE", proposal.getId());
        return Map.of("message", "Promotion completed and payment marked", "status", "COMPLETED", "paymentStatus", "PAID");
    }

    public Map<String, Object> creatorCreatePromotionPostFromApplication(Long applicationId, Map<String, Object> payload) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can create promotion posts");
        }

        BrandCollaborationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!application.getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(application.getStatus());
        if (!"IN_PROMOTION".equals(current) && !"CREATOR_CONFIRMED".equals(current) && !"COMPLETED".equals(current)) {
            throw new RuntimeException("Promotion post can be created after creator accepts promotion");
        }

        User business = application.getOpportunity().getBusinessUser();
        Post post = buildPromotionPost(creator, business, payload, application.getPromotionDetails(),
                application.getPromotionProductImageUrl(), application.getPromotionProductLink(),
                application.getPromotionBusinessPostId());
        Post saved = postRepository.save(post);
        tagBusinessOnPromotionPost(saved, business, creator);
        notificationService.createNotification(business, creator, "COLLAB_PROMOTION_POST_CREATED", saved.getId());
        return Map.of("message", "Promotion post created", "postId", saved.getId());
    }

    public Map<String, Object> creatorCreatePromotionPostFromProposal(Long proposalId, Map<String, Object> payload) {
        User creator = getLoggedInUser();
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can create promotion posts");
        }

        BusinessCollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
        if (!proposal.getCreatorUser().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        String current = normalizeStatus(proposal.getStatus());
        if (!"IN_PROMOTION".equals(current) && !"CREATOR_CONFIRMED".equals(current) && !"COMPLETED".equals(current)) {
            throw new RuntimeException("Promotion post can be created after creator accepts promotion");
        }

        User business = proposal.getBusinessUser();
        Post post = buildPromotionPost(creator, business, payload, proposal.getPromotionDetails(),
                proposal.getPromotionProductImageUrl(), proposal.getPromotionProductLink(),
                proposal.getPromotionBusinessPostId());
        Post saved = postRepository.save(post);
        tagBusinessOnPromotionPost(saved, business, creator);
        notificationService.createNotification(business, creator, "COLLAB_PROMOTION_POST_CREATED", saved.getId());
        return Map.of("message", "Promotion post created", "postId", saved.getId());
    }

    public Map<String, Object> getBusinessRoiFunnel() {
        User business = getLoggedInUser();
        if (business.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business accounts can access this endpoint");
        }

        List<BrandCollaborationOpportunity> opportunities = opportunityRepository
                .findByBusinessUser_IdOrderByCreatedAtDesc(business.getId());
        List<BrandCollaborationApplication> applications = applicationRepository
                .findByOpportunity_BusinessUser_IdOrderByCreatedAtDesc(business.getId());
        List<BusinessCollaborationProposal> proposals = proposalRepository
                .findByBusinessUser_IdOrderByCreatedAtDesc(business.getId());

        long opportunityViews = opportunities.stream().mapToLong(opp -> opp.getViewCount() == null ? 0L : opp.getViewCount()).sum();
        long totalApplications = applications.size();
        long acceptedApplications = applications.stream()
                .filter(app -> "ACCEPTED".equalsIgnoreCase(app.getStatus()))
                .count();
        long inPromotionApplications = applications.stream()
                .filter(app -> "IN_PROMOTION".equalsIgnoreCase(app.getStatus()))
                .count();
        long requestedPromotionApplications = applications.stream()
                .filter(app -> "PROMOTION_REQUESTED".equalsIgnoreCase(app.getStatus()))
                .count();
        long creatorConfirmedApplications = applications.stream()
                .filter(app -> "CREATOR_CONFIRMED".equalsIgnoreCase(app.getStatus()))
                .count();
        long completedApplications = applications.stream()
                .filter(app -> "COMPLETED".equalsIgnoreCase(app.getStatus()))
                .count();
        long paidApplications = applications.stream()
                .filter(app -> "PAID".equalsIgnoreCase(app.getPaymentStatus()))
                .count();
        long sentDirectProposals = proposals.size();
        long acceptedDirectProposals = proposals.stream()
                .filter(proposal -> "ACCEPTED".equalsIgnoreCase(proposal.getStatus()))
                .count();
        long inPromotionDirectProposals = proposals.stream()
                .filter(proposal -> "IN_PROMOTION".equalsIgnoreCase(proposal.getStatus()))
                .count();
        long requestedPromotionDirectProposals = proposals.stream()
                .filter(proposal -> "PROMOTION_REQUESTED".equalsIgnoreCase(proposal.getStatus()))
                .count();
        long creatorConfirmedDirectProposals = proposals.stream()
                .filter(proposal -> "CREATOR_CONFIRMED".equalsIgnoreCase(proposal.getStatus()))
                .count();
        long completedDirectProposals = proposals.stream()
                .filter(proposal -> "COMPLETED".equalsIgnoreCase(proposal.getStatus()))
                .count();
        long paidDirectProposals = proposals.stream()
                .filter(proposal -> "PAID".equalsIgnoreCase(proposal.getPaymentStatus()))
                .count();

        double applyRate = opportunityViews == 0 ? 0.0 : round2(((double) totalApplications * 100.0) / opportunityViews);
        double acceptRate = totalApplications == 0 ? 0.0 : round2(((double) acceptedApplications * 100.0) / totalApplications);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("opportunities", opportunities.size());
        response.put("opportunityViews", opportunityViews);
        response.put("applications", totalApplications);
        response.put("acceptedApplications", acceptedApplications);
        response.put("requestedPromotionApplications", requestedPromotionApplications);
        response.put("inPromotionApplications", inPromotionApplications);
        response.put("creatorConfirmedApplications", creatorConfirmedApplications);
        response.put("completedApplications", completedApplications);
        response.put("paidApplications", paidApplications);
        response.put("sentDirectProposals", sentDirectProposals);
        response.put("acceptedDirectProposals", acceptedDirectProposals);
        response.put("requestedPromotionDirectProposals", requestedPromotionDirectProposals);
        response.put("inPromotionDirectProposals", inPromotionDirectProposals);
        response.put("creatorConfirmedDirectProposals", creatorConfirmedDirectProposals);
        response.put("completedDirectProposals", completedDirectProposals);
        response.put("paidDirectProposals", paidDirectProposals);
        response.put("applyRate", applyRate);
        response.put("acceptRate", acceptRate);
        return response;
    }

    public String exportBusinessRoiFunnelCsv() {
        Map<String, Object> funnel = getBusinessRoiFunnel();
        StringBuilder csv = new StringBuilder();
        csv.append("metric,value\n");
        funnel.forEach((key, value) -> csv.append(key).append(",").append(value).append("\n"));
        return csv.toString();
    }

    private String trimToLength(String value, int maxLen) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > maxLen) {
            return trimmed.substring(0, maxLen);
        }
        return trimmed;
    }

    private void maybeIncrementOpportunityViews(User viewer, List<BrandCollaborationOpportunity> opportunities) {
        if (viewer.getRole() != Handlers.CREATER || opportunities == null || opportunities.isEmpty()) {
            return;
        }

        opportunities.forEach(opportunity -> {
            long current = opportunity.getViewCount() == null ? 0L : opportunity.getViewCount();
            opportunity.setViewCount(current + 1);
        });
        opportunityRepository.saveAll(opportunities);
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String parseString(Object value) {
        return value == null ? null : value.toString();
    }

    private Post buildPromotionPost(User creator,
                                    User business,
                                    Map<String, Object> payload,
                                    String promotionDetails,
                                    String defaultImageUrl,
                                    String defaultProductLink,
                                    Long defaultBusinessPostId) {
        String caption = trimToLength(parseString(payload == null ? null : payload.get("description")), 2000);
        String hashtags = trimToLength(parseString(payload == null ? null : payload.get("hashtags")), 500);
        String mediaUrl = trimToLength(parseString(payload == null ? null : payload.get("mediaUrl")), 2_000_000);
        String mediaType = trimToLength(parseString(payload == null ? null : payload.get("mediaType")), 40);

        if (mediaUrl == null || mediaUrl.isBlank()) {
            mediaUrl = defaultImageUrl;
        }
        if (mediaType == null || mediaType.isBlank()) {
            mediaType = (mediaUrl != null && !mediaUrl.isBlank()) ? "IMAGE" : null;
        }

        String productLink = trimToLength(parseString(payload == null ? null : payload.get("productLink")), 500);
        if (productLink == null || productLink.isBlank()) {
            productLink = defaultProductLink;
        }

        String mention = "@" + business.getUsername();
        StringBuilder description = new StringBuilder();
        if (caption != null && !caption.isBlank()) {
            description.append(caption.trim());
        } else if (promotionDetails != null && !promotionDetails.isBlank()) {
            description.append("Promotion: ").append(promotionDetails.trim());
        } else {
            description.append("Promoting with ").append(mention);
        }
        if (!description.toString().contains(mention)) {
            description.append(" ").append(mention);
        }
        Post post = new Post();
        post.setUser(creator);
        post.setDescription(trimToLength(description.toString(), 2000));
        post.setHashtags(hashtags);
        post.setMediaUrl(mediaUrl);
        post.setMediaType(mediaType);
        post.setProductLink(productLink);
        post.setPublished(true);
        post.setCollabAccepted(true);
        post.setOriginalPostId(defaultBusinessPostId);
        return post;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void notifyCreatorsForOpenOpportunity(BrandCollaborationOpportunity opportunity, User business) {
        if (opportunity == null || business == null) {
            return;
        }

        String targetCategory = trimToLength(opportunity.getCreatorCategory(), 120);
        List<User> creators = userRepository.findByRole(Handlers.CREATER);
        for (User creator : creators) {
            if (creator == null || creator.getId() == null || creator.getId().equals(business.getId())) {
                continue;
            }

            if (targetCategory != null && !targetCategory.isBlank()) {
                String creatorCategory = creator.getCreatorProfile() != null
                        ? trimToLength(creator.getCreatorProfile().getCreatorCategoryLabel(), 120)
                        : null;
                if (creatorCategory != null && !creatorCategory.equalsIgnoreCase(targetCategory)) {
                    continue;
                }
            }

            notificationService.createNotification(creator, business, "COLLAB_OPEN_OPPORTUNITY", opportunity.getId());
        }
    }

    private void tagBusinessOnPromotionPost(Post post, User business, User creator) {
        if (post == null || post.getId() == null || business == null || business.getId() == null) {
            return;
        }

        PostTag tag = new PostTag();
        tag.setPost(post);
        tag.setTaggedUser(business);
        tag.setTaggedBy(creator);
        postTagRepository.save(tag);
    }
}
