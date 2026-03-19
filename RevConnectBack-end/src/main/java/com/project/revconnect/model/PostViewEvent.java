package com.project.revconnect.model;

import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_view_events")
public class PostViewEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private com.project.revconnect.model.Post post;

    @ManyToOne
    @JoinColumn(name = "viewer_id", nullable = false)
    private com.project.revconnect.model.User viewer;

    @Column(name = "watch_seconds")
    private Double watchSeconds = 0.0;

    @Column(name = "completed")
    private boolean completed = false;

    @CreationTimestamp
    @Column(name = "viewed_at", updatable = false)
    private LocalDateTime viewedAt;

    public Long getId() {
        return id;
    }

    public com.project.revconnect.model.Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public com.project.revconnect.model.User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public Double getWatchSeconds() {
        return watchSeconds;
    }

    public void setWatchSeconds(Double watchSeconds) {
        this.watchSeconds = watchSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
}
