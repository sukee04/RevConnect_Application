package com.project.revconnect.controller;

import com.project.revconnect.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/{businessUserId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long businessUserId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String comment,
            @RequestBody(required = false) Map<String, Object> payload) {

        if (rating == null && payload != null && payload.get("rating") instanceof Number number) {
            rating = number.intValue();
        }
        if ((comment == null || comment.isBlank()) && payload != null && payload.get("comment") != null) {
            comment = payload.get("comment").toString();
        }
        if (rating == null || comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().body("rating and comment are required");
        }

        return ResponseEntity.ok(
                service.addReview(businessUserId, rating, comment));
    }

    @PostMapping("/add/{businessUserId}")
    public ResponseEntity<?> addReviewLegacy(
            @PathVariable Long businessUserId,
            @RequestBody Map<String, Object> payload) {
        Object ratingObj = payload != null ? payload.get("rating") : null;
        Object commentObj = payload != null ? payload.get("comment") : null;
        Integer rating = ratingObj instanceof Number number ? number.intValue() : null;
        String comment = commentObj == null ? null : commentObj.toString();
        if (rating == null || comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().body("rating and comment are required");
        }
        return ResponseEntity.ok(service.addReview(businessUserId, rating, comment));
    }

    @GetMapping
    public ResponseEntity<?> getMyBusinessReviews() {
        return ResponseEntity.ok(service.getBusinessReviews());
    }

    @GetMapping("/business/{businessUserId}")
    public ResponseEntity<?> getBusinessReviewsLegacy(@PathVariable Long businessUserId) {
        return ResponseEntity.ok(service.getBusinessReviews());
    }


    @GetMapping("/user/{businessUserId}")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Long businessUserId) {
        try {
            return ResponseEntity.ok(service.getReviewsByBusinessUserId(businessUserId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> delete(@PathVariable Long reviewId) {
        return ResponseEntity.ok(service.deleteReview(reviewId));
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> deleteLegacy(@PathVariable Long reviewId) {
        return ResponseEntity.ok(service.deleteReview(reviewId));
    }
}
