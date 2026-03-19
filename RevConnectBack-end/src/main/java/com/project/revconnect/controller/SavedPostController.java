package com.project.revconnect.controller;

import com.project.revconnect.model.SavedPost;
import com.project.revconnect.service.SavedPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/saved")
public class SavedPostController {

    private final SavedPostService savedPostService;

    public SavedPostController(SavedPostService savedPostService) {
        this.savedPostService = savedPostService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<String> savePost(@PathVariable Long postId) {
        return ResponseEntity.ok(savedPostService.savePost(postId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> unsavePost(@PathVariable Long postId) {
        return ResponseEntity.ok(savedPostService.unsavePost(postId));
    }

    @GetMapping
    public ResponseEntity<List<SavedPost>> getMySavedPosts() {
        return ResponseEntity.ok(savedPostService.getMySavedPosts());
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> getSaveCount(@PathVariable Long postId) {
        return ResponseEntity.ok(savedPostService.getSaveCount(postId));
    }

    @GetMapping("/{postId}/status")
    public ResponseEntity<Boolean> isSavedByUser(@PathVariable Long postId) {
        return ResponseEntity.ok(savedPostService.isSavedByUser(postId));
    }
}
