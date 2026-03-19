package com.project.revconnect.controller;

import com.project.revconnect.model.FollowRequest;
import com.project.revconnect.service.FollowRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/follow", "/follow-requests"})
public class FollowRequestController {

    private final FollowRequestService followRequestService;

    public FollowRequestController(FollowRequestService followRequestService) {
        this.followRequestService = followRequestService;
    }

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long receiverId) {
        return ResponseEntity.ok(java.util.Map.of("message", followRequestService.sendFollowRequest(receiverId)));
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<String> sendRequestLegacy(@PathVariable Long receiverId) {
        return ResponseEntity.ok(followRequestService.sendFollowRequest(receiverId));
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FollowRequest>> getPendingRequests() {
        return ResponseEntity.ok(followRequestService.getPendingRequests());
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FollowRequest>> getSentPendingRequests() {
        return ResponseEntity.ok(followRequestService.getSentPendingRequests());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FollowRequest>> getPendingRequestsLegacy() {
        return ResponseEntity.ok(followRequestService.getPendingRequests());
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FollowRequest>> getSentPendingRequestsLegacy() {
        return ResponseEntity.ok(followRequestService.getSentPendingRequests());
    }

    @PutMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(java.util.Map.of("message", followRequestService.acceptRequest(requestId)));
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<String> acceptRequestLegacy(@PathVariable Long requestId) {
        return ResponseEntity.ok(followRequestService.acceptRequest(requestId));
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(java.util.Map.of("message", followRequestService.rejectRequest(requestId)));
    }
}
