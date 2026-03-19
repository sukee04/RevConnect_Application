package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String mediaUrl;

    private String mediaType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "subscriber_only")
    private boolean subscriberOnly = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Transient private Long userId;
    @Transient private String username;
    @Transient private String userProfilePic;

    public Story() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (expiresAt == null) expiresAt = createdAt.plusHours(24);
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (user != null) {
            this.userId = user.getId();
            this.username = user.getUsername();
            if (user.getUserProfile() != null) this.userProfilePic = user.getUserProfile().getProfilepicURL();
            else if (user.getCreatorProfile() != null) this.userProfilePic = user.getCreatorProfile().getProfilepicURL();
            else if (user.getBusinessProfile() != null) this.userProfilePic = user.getBusinessProfile().getLogoUrl();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    @JsonProperty("isActive")
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @JsonProperty("subscriberOnly")
    public boolean isSubscriberOnly() { return subscriberOnly; }
    public void setSubscriberOnly(boolean subscriberOnly) { this.subscriberOnly = subscriberOnly; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) { this.userId = user.getId(); this.username = user.getUsername(); }
    }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserProfilePic() { return userProfilePic; }
}
