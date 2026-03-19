package com.project.revconnect.model;

import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "following")
public class Following {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne
    @JoinColumn(name = "following_user_id", nullable = false)
    private User followingUser;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Following() {}

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public User getFollowingUser() {
        return followingUser;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFollowingUser(User followingUser) {
        this.followingUser = followingUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
