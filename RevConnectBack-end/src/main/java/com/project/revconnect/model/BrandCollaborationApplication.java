package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.BrandCollaborationOpportunity;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "brand_collaboration_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"opportunity_id", "creator_id"}))
public class BrandCollaborationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "opportunity_id", nullable = false)
    @JsonIgnore
    private BrandCollaborationOpportunity opportunity;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnore
    private User creator;

    @Column(nullable = false, length = 1500)
    private String pitchMessage;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 2000)
    private String promotionDetails;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String promotionProductImageUrl;
    @Column(length = 500)
    private String promotionProductLink;
    private Long promotionBusinessPostId;

    @Column(length = 1500)
    private String creatorConfirmationNote;

    @Column(nullable = false, length = 20)
    private String paymentStatus = "UNPAID";

    private Double paymentAmount;

    @Column(length = 120)
    private String paymentReference;

    private LocalDateTime promotionRequestedAt;
    private LocalDateTime promotionAcceptedAt;
    private LocalDateTime creatorConfirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private Long creatorId;

    @Transient
    private String creatorUsername;

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (creator != null) {
            this.creatorId = creator.getId();
            this.creatorUsername = creator.getUsername();
        }
    }

    public Long getId() {
        return id;
    }

    public BrandCollaborationOpportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(BrandCollaborationOpportunity opportunity) {
        this.opportunity = opportunity;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
        if (creator != null) {
            this.creatorId = creator.getId();
            this.creatorUsername = creator.getUsername();
        }
    }

    public String getPitchMessage() {
        return pitchMessage;
    }

    public void setPitchMessage(String pitchMessage) {
        this.pitchMessage = pitchMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPromotionDetails() {
        return promotionDetails;
    }

    public void setPromotionDetails(String promotionDetails) {
        this.promotionDetails = promotionDetails;
    }

    public String getPromotionProductImageUrl() {
        return promotionProductImageUrl;
    }

    public void setPromotionProductImageUrl(String promotionProductImageUrl) {
        this.promotionProductImageUrl = promotionProductImageUrl;
    }

    public String getPromotionProductLink() {
        return promotionProductLink;
    }

    public void setPromotionProductLink(String promotionProductLink) {
        this.promotionProductLink = promotionProductLink;
    }

    public Long getPromotionBusinessPostId() {
        return promotionBusinessPostId;
    }

    public void setPromotionBusinessPostId(Long promotionBusinessPostId) {
        this.promotionBusinessPostId = promotionBusinessPostId;
    }

    public String getCreatorConfirmationNote() {
        return creatorConfirmationNote;
    }

    public void setCreatorConfirmationNote(String creatorConfirmationNote) {
        this.creatorConfirmationNote = creatorConfirmationNote;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public LocalDateTime getPromotionRequestedAt() {
        return promotionRequestedAt;
    }

    public void setPromotionRequestedAt(LocalDateTime promotionRequestedAt) {
        this.promotionRequestedAt = promotionRequestedAt;
    }

    public LocalDateTime getPromotionAcceptedAt() {
        return promotionAcceptedAt;
    }

    public void setPromotionAcceptedAt(LocalDateTime promotionAcceptedAt) {
        this.promotionAcceptedAt = promotionAcceptedAt;
    }

    public LocalDateTime getCreatorConfirmedAt() {
        return creatorConfirmedAt;
    }

    public void setCreatorConfirmedAt(LocalDateTime creatorConfirmedAt) {
        this.creatorConfirmedAt = creatorConfirmedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }
}
