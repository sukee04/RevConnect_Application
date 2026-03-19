package com.project.revconnect.controller;

import com.project.revconnect.model.CreatorProfile;
import com.project.revconnect.service.CreatorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/creatorProfile")
public class CreatorProfileController {

    private final CreatorProfileService creatorProfileService;

    @Autowired
    public CreatorProfileController(CreatorProfileService creatorProfileService) {
        this.creatorProfileService = creatorProfileService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveCreatorProfile(@RequestBody CreatorProfile profile) {
        try {
            return ResponseEntity.ok(creatorProfileService.saveCreatorProfile(profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyCreatorProfile() {
        try {
            return ResponseEntity.ok(creatorProfileService.getMyCreatorProfile());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/view/{userId}")
    public ResponseEntity<?> getCreatorProfileByUserId(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(creatorProfileService.getCreatorProfileByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/withPosts/{userId}")
    public ResponseEntity<?> getCreatorWithPosts(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(creatorProfileService.getCreatorWithPosts(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByNiche(@RequestParam String niche) {
        try {
            return ResponseEntity.ok(creatorProfileService.searchByNiche(niche));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCreators() {
        try {
            return ResponseEntity.ok(creatorProfileService.getAllCreators());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCreatorProfile() {
        try {
            return ResponseEntity.ok(creatorProfileService.deleteCreatorProfile());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PutMapping("/updatePic")
    public ResponseEntity<?> updateProfilePicture(@RequestBody java.util.Map<String, String> requestBody) {
        try {
            String picUrl = requestBody.get("profilePicUrl");
            if (picUrl == null) {
                picUrl = requestBody.get("profilepicURL");
            }
            return ResponseEntity.ok(creatorProfileService.updateProfilePicture(picUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verified-eligibility")
    public ResponseEntity<?> getMyVerifiedEligibility() {
        try {
            return ResponseEntity.ok(creatorProfileService.getMyVerifiedEligibility());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
