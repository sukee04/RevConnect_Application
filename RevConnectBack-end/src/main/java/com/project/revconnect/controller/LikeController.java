package com.project.revconnect.controller;

import com.project.revconnect.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<String> toggleLike(@PathVariable Long postId) {
        likeService.toggleLike(postId);
        return ResponseEntity.ok("Like toggled successfully");
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{postId}/status")
    public ResponseEntity<Boolean> isLiked(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.isLikedByUser(postId));
    }

    @GetMapping("/{postId}/users")
    public ResponseEntity<List<Map<String, Object>>> getLikedUsers(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikedUsers(postId));
    }
}
