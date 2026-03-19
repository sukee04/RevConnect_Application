package com.project.revconnect.controller;

import com.project.revconnect.model.BrandCollaborationApplication;
import com.project.revconnect.model.BrandCollaborationOpportunity;
import com.project.revconnect.model.BusinessCollaborationProposal;
import com.project.revconnect.service.BrandCollaborationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/creator/marketplace")
public class BrandCollaborationController {

    private final BrandCollaborationService brandCollaborationService;

    public BrandCollaborationController(BrandCollaborationService brandCollaborationService) {
        this.brandCollaborationService = brandCollaborationService;
    }

    @PostMapping("/opportunities")
    public ResponseEntity<?> createOpportunity(@RequestBody BrandCollaborationOpportunity request) {
        return ResponseEntity.ok(brandCollaborationService.createOpportunity(request));
    }

    @GetMapping("/opportunities")
    public ResponseEntity<List<BrandCollaborationOpportunity>> listOpenOpportunities(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(brandCollaborationService.listOpenOpportunities(category));
    }

    @GetMapping("/opportunities/business/me")
    public ResponseEntity<List<BrandCollaborationOpportunity>> getMyBusinessOpportunities() {
        return ResponseEntity.ok(brandCollaborationService.getMyBusinessOpportunities());
    }

    @PostMapping("/opportunities/{opportunityId}/apply")
    public ResponseEntity<?> applyToOpportunity(@PathVariable Long opportunityId,
                                                @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.applyToOpportunity(opportunityId, payload));
    }

    @GetMapping("/applications/me")
    public ResponseEntity<List<BrandCollaborationApplication>> getMyApplications() {
        return ResponseEntity.ok(brandCollaborationService.getMyApplications());
    }

    @GetMapping("/opportunities/{opportunityId}/applications")
    public ResponseEntity<List<BrandCollaborationApplication>> getApplicationsForOpportunity(
            @PathVariable Long opportunityId) {
        return ResponseEntity.ok(brandCollaborationService.getApplicationsForOpportunity(opportunityId));
    }

    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId,
                                                     @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.updateApplicationStatus(applicationId, payload));
    }

    @PutMapping("/applications/{applicationId}/promotion/start")
    public ResponseEntity<?> startApplicationPromotion(@PathVariable Long applicationId) {
        return ResponseEntity.ok(brandCollaborationService.startApplicationPromotion(applicationId));
    }

    @PutMapping("/applications/{applicationId}/promotion/complete")
    public ResponseEntity<?> completeApplicationPromotion(@PathVariable Long applicationId) {
        return ResponseEntity.ok(brandCollaborationService.completeApplicationPromotion(applicationId));
    }

    @PutMapping("/applications/{applicationId}/promotion/request")
    public ResponseEntity<?> requestApplicationPromotion(@PathVariable Long applicationId,
                                                         @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.requestApplicationPromotion(applicationId, payload));
    }

    @PutMapping("/applications/{applicationId}/promotion/accept")
    public ResponseEntity<?> creatorAcceptApplicationPromotion(@PathVariable Long applicationId) {
        return ResponseEntity.ok(brandCollaborationService.creatorAcceptApplicationPromotion(applicationId));
    }

    @PutMapping("/applications/{applicationId}/promotion/confirm")
    public ResponseEntity<?> creatorConfirmApplicationPromotion(@PathVariable Long applicationId,
                                                                @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.creatorConfirmApplicationPromotion(applicationId, payload));
    }

    @PutMapping("/applications/{applicationId}/promotion/complete-pay")
    public ResponseEntity<?> completeApplicationPromotionAndPay(@PathVariable Long applicationId,
                                                                @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(brandCollaborationService.completeApplicationPromotionAndPay(applicationId, payload));
    }

    @PostMapping("/applications/{applicationId}/promotion/create-post")
    public ResponseEntity<?> creatorCreatePromotionPostFromApplication(@PathVariable Long applicationId,
                                                                       @RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(brandCollaborationService.creatorCreatePromotionPostFromApplication(applicationId, payload));
    }

    @PutMapping("/opportunities/{opportunityId}/close")
    public ResponseEntity<?> closeOpportunity(@PathVariable Long opportunityId) {
        return ResponseEntity.ok(brandCollaborationService.closeOpportunity(opportunityId));
    }

    @PostMapping("/proposals/send")
    public ResponseEntity<BusinessCollaborationProposal> sendDirectProposal(
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(brandCollaborationService.sendDirectProposal(payload));
    }

    @GetMapping("/proposals/sent/me")
    public ResponseEntity<List<BusinessCollaborationProposal>> getMySentDirectProposals() {
        return ResponseEntity.ok(brandCollaborationService.getMySentDirectProposals());
    }

    @GetMapping("/proposals/received/me")
    public ResponseEntity<List<BusinessCollaborationProposal>> getMyReceivedDirectProposals() {
        return ResponseEntity.ok(brandCollaborationService.getMyReceivedDirectProposals());
    }

    @PutMapping("/proposals/{proposalId}/status")
    public ResponseEntity<?> updateDirectProposalStatus(@PathVariable Long proposalId,
                                                        @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.updateDirectProposalStatus(proposalId, payload));
    }

    @PutMapping("/proposals/{proposalId}/promotion/start")
    public ResponseEntity<?> startDirectProposalPromotion(@PathVariable Long proposalId) {
        return ResponseEntity.ok(brandCollaborationService.startDirectProposalPromotion(proposalId));
    }

    @PutMapping("/proposals/{proposalId}/promotion/complete")
    public ResponseEntity<?> completeDirectProposalPromotion(@PathVariable Long proposalId) {
        return ResponseEntity.ok(brandCollaborationService.completeDirectProposalPromotion(proposalId));
    }

    @PutMapping("/proposals/{proposalId}/promotion/request")
    public ResponseEntity<?> requestDirectProposalPromotion(@PathVariable Long proposalId,
                                                            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.requestDirectProposalPromotion(proposalId, payload));
    }

    @PutMapping("/proposals/{proposalId}/promotion/accept")
    public ResponseEntity<?> creatorAcceptDirectProposalPromotion(@PathVariable Long proposalId) {
        return ResponseEntity.ok(brandCollaborationService.creatorAcceptDirectProposalPromotion(proposalId));
    }

    @PutMapping("/proposals/{proposalId}/promotion/confirm")
    public ResponseEntity<?> creatorConfirmDirectProposalPromotion(@PathVariable Long proposalId,
                                                                   @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(brandCollaborationService.creatorConfirmDirectProposalPromotion(proposalId, payload));
    }

    @PutMapping("/proposals/{proposalId}/promotion/complete-pay")
    public ResponseEntity<?> completeDirectProposalPromotionAndPay(@PathVariable Long proposalId,
                                                                   @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(brandCollaborationService.completeDirectProposalPromotionAndPay(proposalId, payload));
    }

    @PostMapping("/proposals/{proposalId}/promotion/create-post")
    public ResponseEntity<?> creatorCreatePromotionPostFromProposal(@PathVariable Long proposalId,
                                                                    @RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(brandCollaborationService.creatorCreatePromotionPostFromProposal(proposalId, payload));
    }

    @GetMapping("/analytics/business/funnel")
    public ResponseEntity<?> getBusinessRoiFunnel() {
        return ResponseEntity.ok(brandCollaborationService.getBusinessRoiFunnel());
    }

    @GetMapping("/analytics/business/funnel/export")
    public ResponseEntity<String> exportBusinessRoiFunnelCsv() {
        String csv = brandCollaborationService.exportBusinessRoiFunnelCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=business_roi_funnel.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
