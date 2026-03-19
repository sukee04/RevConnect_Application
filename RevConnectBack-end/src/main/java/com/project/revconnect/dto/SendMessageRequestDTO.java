package com.project.revconnect.dto;

import com.project.revconnect.dto.SharedPostPayloadDTO;

public class SendMessageRequestDTO {
    private String content;
    private SharedPostPayloadDTO sharedPost;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SharedPostPayloadDTO getSharedPost() {
        return sharedPost;
    }

    public void setSharedPost(SharedPostPayloadDTO sharedPost) {
        this.sharedPost = sharedPost;
    }
}
