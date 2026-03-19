package com.project.revconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;


    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;


    @Transient private Long userId;
    @Transient private String username;
    @Transient private Long postId;

    public Comment() {}

    public Comment(String content, User user, Post post) {
        this.content = content;
        this.user = user;
        this.post = post;
        this.createdAt = LocalDateTime.now();
        if (user != null) { this.userId = user.getId(); this.username = user.getUsername(); }
        if (post != null) { this.postId = post.getId(); }
    }

    @PostLoad
    @PostPersist
    public void populateTransient() {
        if (user != null) { this.userId = user.getId(); this.username = user.getUsername(); }
        if (post != null) { this.postId = post.getId(); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) { this.userId = user.getId(); this.username = user.getUsername(); }
    }
    public Post getPost() { return post; }
    public void setPost(Post post) {
        this.post = post;
        if (post != null) { this.postId = post.getId(); }
    }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getPostId() { return postId; }
}
