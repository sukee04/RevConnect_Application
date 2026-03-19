package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "creator_subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = { "creator_id", "subscriber_id" }))
public class CreatorSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User creator;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User subscriber;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private Long creatorId;

    @Transient
    private String creatorUsername;

    public Long getId() {
        return id;
    }

    public com.project.revconnect.model.User getCreator() {
        return creator;
    }

    public void setCreator(com.project.revconnect.model.User creator) {
        this.creator = creator;
        if (creator != null) {
            this.creatorId = creator.getId();
            this.creatorUsername = creator.getUsername();
        }
    }

    public com.project.revconnect.model.User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
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

    public Long getCreatorId() {
        return creatorId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (creator != null) {
            this.creatorId = creator.getId();
            this.creatorUsername = creator.getUsername();
        }
    }
}

