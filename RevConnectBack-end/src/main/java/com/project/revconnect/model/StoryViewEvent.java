package com.project.revconnect.model;

import com.project.revconnect.model.Story;
import com.project.revconnect.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_view_events")
public class StoryViewEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer;

    @Column(name = "tap_through")
    private boolean tapThrough = false;

    @CreationTimestamp
    @Column(name = "viewed_at", updatable = false)
    private LocalDateTime viewedAt;

    public Long getId() {
        return id;
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public boolean isTapThrough() {
        return tapThrough;
    }

    public void setTapThrough(boolean tapThrough) {
        this.tapThrough = tapThrough;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
}
