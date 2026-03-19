package com.project.revconnect.dto;

import java.time.LocalDateTime;

public class MessageResponseDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private Long sharedPostId;
    private String sharedPostMediaUrl;
    private String sharedPostMediaType;
    private String sharedPostDescription;
    private String sharedPostAuthorUsername;
    private LocalDateTime timestamp;

    public MessageResponseDTO() {
    }

    public MessageResponseDTO(Long id, Long senderId, String senderName, Long receiverId, String receiverName,
            String content, Long sharedPostId, String sharedPostMediaUrl, String sharedPostMediaType,
            String sharedPostDescription, String sharedPostAuthorUsername, LocalDateTime timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.sharedPostId = sharedPostId;
        this.sharedPostMediaUrl = sharedPostMediaUrl;
        this.sharedPostMediaType = sharedPostMediaType;
        this.sharedPostDescription = sharedPostDescription;
        this.sharedPostAuthorUsername = sharedPostAuthorUsername;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSharedPostId() {
        return sharedPostId;
    }

    public void setSharedPostId(Long sharedPostId) {
        this.sharedPostId = sharedPostId;
    }

    public String getSharedPostMediaUrl() {
        return sharedPostMediaUrl;
    }

    public void setSharedPostMediaUrl(String sharedPostMediaUrl) {
        this.sharedPostMediaUrl = sharedPostMediaUrl;
    }

    public String getSharedPostMediaType() {
        return sharedPostMediaType;
    }

    public void setSharedPostMediaType(String sharedPostMediaType) {
        this.sharedPostMediaType = sharedPostMediaType;
    }

    public String getSharedPostDescription() {
        return sharedPostDescription;
    }

    public void setSharedPostDescription(String sharedPostDescription) {
        this.sharedPostDescription = sharedPostDescription;
    }

    public String getSharedPostAuthorUsername() {
        return sharedPostAuthorUsername;
    }

    public void setSharedPostAuthorUsername(String sharedPostAuthorUsername) {
        this.sharedPostAuthorUsername = sharedPostAuthorUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
