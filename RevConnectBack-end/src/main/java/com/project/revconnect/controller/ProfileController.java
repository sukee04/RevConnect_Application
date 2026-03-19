package com.project.revconnect.controller;

import com.project.revconnect.model.UserProfile;
import com.project.revconnect.service.ProfileService;
import com.project.revconnect.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/userProfile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;


    @PostMapping("/addUserProfile/{userId}")
    public ResponseEntity<?> addProfile(@PathVariable Long userId,
                                        @RequestBody UserProfile profile) {
        try {
            return ResponseEntity.ok(profileService.addProfile(userId, profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> myProfile() {
        try {
            String currentUsername = AuthUtil.getLoggedInUsername();
            if (currentUsername == null) return ResponseEntity.status(401).body("Not authenticated");
            return ResponseEntity.ok(profileService.getUserByName(currentUsername));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @GetMapping("/view/{username}")
    public ResponseEntity<?> viewProfile(@PathVariable String username) {
        try {
            return ResponseEntity.ok(profileService.getUserByName(username));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/updatePic")
    public ResponseEntity<?> updateProfilePicture(@RequestBody Map<String, String> requestBody) {
        try {
            String currentUsername = AuthUtil.getLoggedInUsername();
            if (currentUsername == null) return ResponseEntity.status(401).body("Not authenticated");

            String picUrl = requestBody.get("profilePicUrl");
            if (picUrl == null) picUrl = requestBody.get("profilepicURL");
            if (picUrl == null) picUrl = requestBody.get("profilepicUrl");
            if (picUrl == null) picUrl = requestBody.get("logoUrl");

            return ResponseEntity.ok(profileService.updateProfilePicture(currentUsername, picUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/privacy")
    public ResponseEntity<?> updatePrivacy(@RequestBody Map<String, Boolean> requestBody) {
        try {
            String currentUsername = AuthUtil.getLoggedInUsername();
            if (currentUsername == null) return ResponseEntity.status(401).body("Not authenticated");

            Boolean isPublic = requestBody.get("isPublic");
            if (isPublic == null) isPublic = requestBody.getOrDefault("public", true);

            return ResponseEntity.ok(profileService.updatePrivacy(currentUsername, isPublic));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
