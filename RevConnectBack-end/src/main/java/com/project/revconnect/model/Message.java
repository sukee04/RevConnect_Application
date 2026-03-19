package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private com.project.revconnect.model.User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private com.project.revconnect.model.User receiver;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "shared_post_id")
    private Long sharedPostId;

    @Lob
    @Column(name = "shared_post_media_url", columnDefinition = "LONGTEXT")
    private String sharedPostMediaUrl;

    @Column(name = "shared_post_media_type", length = 40)
    private String sharedPostMediaType;

    @Column(name = "shared_post_description", length = 2000)
    private String sharedPostDescription;

    @Column(name = "shared_post_author_username", length = 120)
    private String sharedPostAuthorUsername;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    public Message() {
    }

    public Message(com.project.revconnect.model.User sender, com.project.revconnect.model.User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public com.project.revconnect.model.User getSender() {
        return sender;
    }

    public void setSender(com.project.revconnect.model.User sender) {
        this.sender = sender;
    }

    public com.project.revconnect.model.User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    @JsonProperty("isRead")
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
