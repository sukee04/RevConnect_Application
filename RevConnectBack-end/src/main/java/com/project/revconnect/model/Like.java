package com.project.revconnect.model;

import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private com.project.revconnect.model.User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private com.project.revconnect.model.Post post;

    public Like() {
    }

    public Like(com.project.revconnect.model.User user, com.project.revconnect.model.Post post) {
        this.user = user;
        this.post = post;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public com.project.revconnect.model.User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public com.project.revconnect.model.Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
