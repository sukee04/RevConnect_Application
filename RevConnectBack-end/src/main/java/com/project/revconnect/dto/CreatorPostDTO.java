package com.project.revconnect.dto;

import com.project.revconnect.model.CreatorProfile;
import com.project.revconnect.model.Post;

import java.util.List;

public class CreatorPostDTO {

    private CreatorProfile creatorProfile;
    private List<Post> posts;

    public CreatorPostDTO(CreatorProfile creatorProfile, List<Post> posts) {
        this.creatorProfile = creatorProfile;
        this.posts = posts;
    }

    public CreatorProfile getCreatorProfile() { return creatorProfile; }
    public void setCreatorProfile(CreatorProfile creatorProfile) { this.creatorProfile = creatorProfile; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
}