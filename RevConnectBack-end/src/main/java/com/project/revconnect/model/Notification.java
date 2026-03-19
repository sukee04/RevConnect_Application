package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User recipient;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User actor;


    @Transient private Long recipientId;
    @Transient private Long actorId;
    @Transient private String actorUsername;
    @Transient private String actorProfilePic;

    @Column(nullable = false)
    private String type;

    private Long referenceId;

    @Column(name = "is_read")
    private boolean read = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(com.project.revconnect.model.User recipient, com.project.revconnect.model.User actor, String type, Long referenceId) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.referenceId = referenceId;
        this.read = false;
        populateTransientFields();
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (recipient != null) this.recipientId = recipient.getId();
        if (actor != null) {
            this.actorId = actor.getId();
            this.actorUsername = actor.getUsername();
            if (actor.getUserProfile() != null) this.actorProfilePic = actor.getUserProfile().getProfilepicURL();
            else if (actor.getCreatorProfile() != null) this.actorProfilePic = actor.getCreatorProfile().getProfilepicURL();
            else if (actor.getBusinessProfile() != null) this.actorProfilePic = actor.getBusinessProfile().getLogoUrl();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public com.project.revconnect.model.User getRecipient() { return recipient; }
    public void setRecipient(com.project.revconnect.model.User recipient) { this.recipient = recipient; if (recipient != null) this.recipientId = recipient.getId(); }
    public com.project.revconnect.model.User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; if (actor != null) { this.actorId = actor.getId(); this.actorUsername = actor.getUsername(); } }
    public Long getRecipientId() { return recipientId; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public String getActorProfilePic() { return actorProfilePic; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    @JsonProperty("isRead")
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
