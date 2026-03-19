package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User receiver;

    @Transient private Long senderId;
    @Transient private String senderUsername;
    @Transient private String senderProfilePic;
    @Transient private Long receiverId;
    @Transient private String receiverUsername;

    @Column(nullable = false)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public FollowRequest() {}

    public FollowRequest(com.project.revconnect.model.User sender, com.project.revconnect.model.User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = "PENDING";
        populateTransientFields();
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void populateTransientFields() {
        if (sender != null) {
            this.senderId = sender.getId();
            this.senderUsername = sender.getUsername();
            if (sender.getUserProfile() != null) this.senderProfilePic = sender.getUserProfile().getProfilepicURL();
        }
        if (receiver != null) {
            this.receiverId = receiver.getId();
            this.receiverUsername = receiver.getUsername();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public com.project.revconnect.model.User getSender() { return sender; }
    public void setSender(com.project.revconnect.model.User sender) { this.sender = sender; if (sender != null) { this.senderId = sender.getId(); this.senderUsername = sender.getUsername(); } }
    public com.project.revconnect.model.User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; if (receiver != null) { this.receiverId = receiver.getId(); this.receiverUsername = receiver.getUsername(); } }
    public Long getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderProfilePic() { return senderProfilePic; }
    public Long getReceiverId() { return receiverId; }
    public String getReceiverUsername() { return receiverUsername; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
