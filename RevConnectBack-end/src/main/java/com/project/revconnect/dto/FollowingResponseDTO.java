package com.project.revconnect.dto;

public class FollowingResponseDTO {

    private Long followingId;
    private String followingUsername;

    public FollowingResponseDTO() {}

    public FollowingResponseDTO(Long followingId, String followingUsername) {
        this.followingId = followingId;
        this.followingUsername = followingUsername;
    }

    public Long getFollowingId() {
        return followingId;
    }

    public String getFollowingUsername() {
        return followingUsername;
    }
}
