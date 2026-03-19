package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String comment;
    private LocalDateTime createdAt;


    @ManyToOne
    @JoinColumn(name = "business_user_id", nullable = false)
    @JsonIgnore
    private User businessUser;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnore
    private User reviewer;


    @Transient private Long businessUserId;
    @Transient private String businessUsername;
    @Transient private Long reviewerId;
    @Transient private String reviewerUsername;
    @Transient private String reviewerProfilePic;

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    @PostLoad
    @PostPersist
    public void populateTransient() {
        if (businessUser != null) {
            this.businessUserId = businessUser.getId();
            this.businessUsername = businessUser.getUsername();
        }
        if (reviewer != null) {
            this.reviewerId = reviewer.getId();
            this.reviewerUsername = reviewer.getUsername();
            if (reviewer.getUserProfile() != null) {
                this.reviewerProfilePic = reviewer.getUserProfile().getProfilepicURL();
            }
        }
    }

    public Long getId() { return id; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getBusinessUser() { return businessUser; }
    public User getReviewer() { return reviewer; }
    public Long getBusinessUserId() { return businessUserId; }
    public String getBusinessUsername() { return businessUsername; }
    public Long getReviewerId() { return reviewerId; }
    public String getReviewerUsername() { return reviewerUsername; }
    public String getReviewerProfilePic() { return reviewerProfilePic; }

    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setBusinessUser(User businessUser) {
        this.businessUser = businessUser;
        if (businessUser != null) { this.businessUserId = businessUser.getId(); this.businessUsername = businessUser.getUsername(); }
    }
    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
        if (reviewer != null) { this.reviewerId = reviewer.getId(); this.reviewerUsername = reviewer.getUsername(); }
    }
}
