package com.project.revconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PostResponseDTO {
    private Long postId;
    private String description;
    private Long userId;
    private String userName;
    private String authorProfilePicture;
    private String mediaUrl;
    private String mediaType;
    private String productLink;
    private java.time.LocalDateTime scheduledAt;
    private boolean isPinned;
    private boolean isPublished;
    private boolean collabAccepted;
    private Long collaboratorId;
    private String collaboratorUsername;
    private String seriesName;
    private Integer seriesOrder;
    private java.time.LocalDateTime createdAt;
    private long likeCount;
    private long commentCount;
    private long saveCount;

    public PostResponseDTO(Long postId, String description, Long userId, String userName) {
        this.postId = postId;
        this.description = description;
        this.userId = userId;
        this.userName = userName;
    }

    public PostResponseDTO() {
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAuthorProfilePicture() {
        return authorProfilePicture;
    }

    public void setAuthorProfilePicture(String authorProfilePicture) {
        this.authorProfilePicture = authorProfilePicture;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getProductLink() {
        return productLink;
    }

    public void setProductLink(String productLink) {
        this.productLink = productLink;
    }

    public java.time.LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(java.time.LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    @JsonProperty("isPinned")
    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    @JsonProperty("isPublished")
    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public boolean isCollabAccepted() {
        return collabAccepted;
    }

    public void setCollabAccepted(boolean collabAccepted) {
        this.collabAccepted = collabAccepted;
    }

    public Long getCollaboratorId() {
        return collaboratorId;
    }

    public void setCollaboratorId(Long collaboratorId) {
        this.collaboratorId = collaboratorId;
    }

    public String getCollaboratorUsername() {
        return collaboratorUsername;
    }

    public void setCollaboratorUsername(String collaboratorUsername) {
        this.collaboratorUsername = collaboratorUsername;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public Integer getSeriesOrder() {
        return seriesOrder;
    }

    public void setSeriesOrder(Integer seriesOrder) {
        this.seriesOrder = seriesOrder;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(long saveCount) {
        this.saveCount = saveCount;
    }
}
