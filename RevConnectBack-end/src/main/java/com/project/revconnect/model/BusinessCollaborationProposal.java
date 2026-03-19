package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_collaboration_proposals")
public class BusinessCollaborationProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_user_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User businessUser;

    @ManyToOne
    @JoinColumn(name = "creator_user_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User creatorUser;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    private Double budget;

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

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    private Long businessUserId;

    @Transient
    private String businessUsername;

    @Transient
    private Long creatorUserId;

    @Transient
    private String creatorUsername;

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (businessUser != null) {
            this.businessUserId = businessUser.getId();
            this.businessUsername = businessUser.getUsername();
        }
        if (creatorUser != null) {
            this.creatorUserId = creatorUser.getId();
            this.creatorUsername = creatorUser.getUsername();
        }
    }

    public Long getId() {
        return id;
    }

    public com.project.revconnect.model.User getBusinessUser() {
        return businessUser;
    }

    public void setBusinessUser(com.project.revconnect.model.User businessUser) {
        this.businessUser = businessUser;
        if (businessUser != null) {
            this.businessUserId = businessUser.getId();
            this.businessUsername = businessUser.getUsername();
        }
    }

    public com.project.revconnect.model.User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
        if (creatorUser != null) {
            this.creatorUserId = creatorUser.getId();
            this.creatorUsername = creatorUser.getUsername();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public Long getBusinessUserId() {
        return businessUserId;
    }

    public String getBusinessUsername() {
        return businessUsername;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }
}
