package com.project.revconnect.controller;

import com.project.revconnect.service.CreatorSubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/creator/subscriptions")
public class CreatorSubscriptionController {

    private final CreatorSubscriptionService creatorSubscriptionService;

    public CreatorSubscriptionController(CreatorSubscriptionService creatorSubscriptionService) {
        this.creatorSubscriptionService = creatorSubscriptionService;
    }

    @PostMapping("/{creatorId}")
    public ResponseEntity<?> subscribe(@PathVariable Long creatorId) {
        return ResponseEntity.ok(creatorSubscriptionService.subscribeToCreator(creatorId));
    }

    @DeleteMapping("/{creatorId}")
    public ResponseEntity<?> unsubscribe(@PathVariable Long creatorId) {
        return ResponseEntity.ok(creatorSubscriptionService.unsubscribeFromCreator(creatorId));
    }

    @GetMapping("/{creatorId}/status")
    public ResponseEntity<?> status(@PathVariable Long creatorId) {
        return ResponseEntity.ok(creatorSubscriptionService.getSubscriptionStatus(creatorId));
    }

    @GetMapping("/me")
    public ResponseEntity<?> mySubscriptions() {
        return ResponseEntity.ok(creatorSubscriptionService.getMySubscriptions());
    }
}

