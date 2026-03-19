package com.project.revconnect.controller;

import com.project.revconnect.model.Story;
import com.project.revconnect.service.StoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping({"", "/create"})
    public ResponseEntity<Story> createStory(@RequestBody Story story) {
        return ResponseEntity.ok(storyService.createStory(story));
    }

    @GetMapping({"/me", "/my"})
    public ResponseEntity<List<Story>> getMyStories() {
        return ResponseEntity.ok(storyService.getMyStories());
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Story>> getFeedStories() {
        return ResponseEntity.ok(storyService.getFeedStories());
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public ResponseEntity<String> deleteStory(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.deleteStory(id));
    }

    @GetMapping("/{id}/viewers")
    public ResponseEntity<List<java.util.Map<String, Object>>> getStoryViewers(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getStoryViewers(id));
    }
}
