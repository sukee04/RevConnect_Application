package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_posts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class SavedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private com.project.revconnect.model.User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private com.project.revconnect.model.Post post;

    @Transient private Long userId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public SavedPost() {}

    public SavedPost(com.project.revconnect.model.User user, com.project.revconnect.model.Post post) {
        this.user = user;
        this.post = post;
        if (user != null) this.userId = user.getId();
    }

    @PostLoad
    @PostPersist
    public void populateTransient() {
        if (user != null) this.userId = user.getId();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public com.project.revconnect.model.User getUser() { return user; }
    public void setUser(User user) { this.user = user; if (user != null) this.userId = user.getId(); }
    public com.project.revconnect.model.Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
