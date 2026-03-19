package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "brand_collaboration_opportunities")
public class BrandCollaborationOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_user_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User businessUser;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 120)
    private String creatorCategory;

    private Double minBudget;
    private Double maxBudget;
    private Long viewCount = 0L;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private Long businessUserId;

    @Transient
    private String businessUsername;

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (businessUser != null) {
            this.businessUserId = businessUser.getId();
            this.businessUsername = businessUser.getUsername();
        }
    }

    public Long getId() {
        return id;
    }

    public com.project.revconnect.model.User getBusinessUser() {
        return businessUser;
    }

    public void setBusinessUser(User businessUser) {
        this.businessUser = businessUser;
        if (businessUser != null) {
            this.businessUserId = businessUser.getId();
            this.businessUsername = businessUser.getUsername();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorCategory() {
        return creatorCategory;
    }

    public void setCreatorCategory(String creatorCategory) {
        this.creatorCategory = creatorCategory;
    }

    public Double getMinBudget() {
        return minBudget;
    }

    public void setMinBudget(Double minBudget) {
        this.minBudget = minBudget;
    }

    public Double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(Double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getBusinessUserId() {
        return businessUserId;
    }

    public String getBusinessUsername() {
        return businessUsername;
    }
}
