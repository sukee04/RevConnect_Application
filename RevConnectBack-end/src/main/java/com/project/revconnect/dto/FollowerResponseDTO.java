package com.project.revconnect.dto;

public class FollowerResponseDTO {

    private Long followerId;
    private String followerUsername;

    public FollowerResponseDTO() {}

    public FollowerResponseDTO(Long followerId, String followerUsername) {
        this.followerId = followerId;
        this.followerUsername = followerUsername;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public String getFollowerUsername() {
        return followerUsername;
    }
}
