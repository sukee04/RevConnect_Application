package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.revconnect.model.Comment;
import com.project.revconnect.model.Like;
import com.project.revconnect.model.SavedPost;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private boolean boosted;
    private double boostBudget;
    private String boostStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String mediaUrl;

    private String mediaType;
    private String hashtags;
    @Column(length = 1200)
    private String productLink;
    private Long originalPostId;
    private LocalDateTime scheduledAt;

    @Column(name = "is_published", columnDefinition = "boolean default true")
    private Boolean published = true;

    @Column(name = "is_pinned")
    private boolean isPinned = false;

    @ManyToOne
    @JoinColumn(name = "collaborator_user_id")
    @JsonIgnore
    private User collaborator;

    @Column(name = "collab_accepted", columnDefinition = "boolean default true")
    private Boolean collabAccepted = true;

    @Column(name = "series_name", length = 140)
    private String seriesName;

    @Column(name = "series_order")
    private Integer seriesOrder;

    @Transient
    private Long collaboratorId;

    @Transient
    private String collaboratorUsername;

    @Transient
    private List<String> taggedUsernames;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Like> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SavedPost> savedPosts;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostTag> postTags;

    public Post() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isBoosted() { return boosted; }
    public void setBoosted(boolean boosted) { this.boosted = boosted; }
    public double getBoostBudget() { return boostBudget; }
    public void setBoostBudget(double boostBudget) { this.boostBudget = boostBudget; }
    public String getBoostStatus() { return boostStatus; }
    public void setBoostStatus(String boostStatus) { this.boostStatus = boostStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }
    public String getProductLink() { return productLink; }
    public void setProductLink(String productLink) { this.productLink = productLink; }
    public Long getOriginalPostId() { return originalPostId; }
    public void setOriginalPostId(Long originalPostId) { this.originalPostId = originalPostId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    @JsonProperty("isPublished")
    public boolean isPublished() { return published == null || published; }
    public void setPublished(Boolean published) { this.published = published; }

    @JsonProperty("isPinned")
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public User getCollaborator() { return collaborator; }
    public void setCollaborator(User collaborator) { this.collaborator = collaborator; }

    @JsonProperty("collabAccepted")
    public boolean isCollabAccepted() { return collabAccepted == null || collabAccepted; }
    public void setCollabAccepted(Boolean collabAccepted) { this.collabAccepted = collabAccepted; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public Integer getSeriesOrder() { return seriesOrder; }
    public void setSeriesOrder(Integer seriesOrder) { this.seriesOrder = seriesOrder; }

    public Long getCollaboratorId() { return collaboratorId; }
    public void setCollaboratorId(Long collaboratorId) { this.collaboratorId = collaboratorId; }

    public String getCollaboratorUsername() { return collaboratorUsername; }
    public void setCollaboratorUsername(String collaboratorUsername) { this.collaboratorUsername = collaboratorUsername; }
    public List<String> getTaggedUsernames() { return taggedUsernames; }
    public void setTaggedUsernames(List<String> taggedUsernames) { this.taggedUsernames = taggedUsernames; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
