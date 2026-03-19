package com.project.revconnect.controller;

import com.project.revconnect.service.CreatorAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/creator/analytics")
public class CreatorAnalyticsController {

    private final CreatorAnalyticsService creatorAnalyticsService;

    public CreatorAnalyticsController(CreatorAnalyticsService creatorAnalyticsService) {
        this.creatorAnalyticsService = creatorAnalyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(creatorAnalyticsService.getDashboard());
    }

    @PostMapping("/track/post/{postId}")
    public ResponseEntity<?> trackPostView(@PathVariable Long postId,
                                           @RequestBody(required = false) Map<String, Object> request) {
        Double watchSeconds = null;
        Boolean completed = null;
        if (request != null) {
            Object ws = request.get("watchSeconds");
            if (ws instanceof Number num) {
                watchSeconds = num.doubleValue();
            } else if (ws instanceof String str && !str.isBlank()) {
                try {
                    watchSeconds = Double.parseDouble(str);
                } catch (NumberFormatException ignored) {
                }
            }

            Object completedValue = request.get("completed");
            if (completedValue instanceof Boolean b) {
                completed = b;
            } else if (completedValue instanceof String s) {
                completed = Boolean.parseBoolean(s);
            }
        }
        return ResponseEntity.ok(creatorAnalyticsService.recordPostView(postId, watchSeconds, completed));
    }

    @PostMapping("/track/story/{storyId}")
    public ResponseEntity<?> trackStoryView(@PathVariable Long storyId,
                                            @RequestBody(required = false) Map<String, Object> request) {
        Boolean tapThrough = null;
        if (request != null) {
            Object tapValue = request.get("tapThrough");
            if (tapValue instanceof Boolean b) {
                tapThrough = b;
            } else if (tapValue instanceof String s) {
                tapThrough = Boolean.parseBoolean(s);
            }
        }
        return ResponseEntity.ok(creatorAnalyticsService.recordStoryView(storyId, tapThrough));
    }
}
