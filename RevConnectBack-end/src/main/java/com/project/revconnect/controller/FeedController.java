package com.project.revconnect.controller;

import com.project.revconnect.dto.PostResponseDTO;
import com.project.revconnect.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/home")
    public ResponseEntity<List<PostResponseDTO>> getHomeFeed() {
        return ResponseEntity.ok(feedService.getHomeFeed());
    }

    @GetMapping("/explore")
    public ResponseEntity<List<PostResponseDTO>> getExploreFeed() {
        return ResponseEntity.ok(feedService.getExploreFeed());
    }
}
