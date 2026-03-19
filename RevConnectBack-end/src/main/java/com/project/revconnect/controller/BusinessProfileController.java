package com.project.revconnect.controller;

import com.project.revconnect.model.BusinessProfile;
import com.project.revconnect.service.BusinessProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/profile")
public class BusinessProfileController {

    private final BusinessProfileService service;

    public BusinessProfileController(BusinessProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody BusinessProfile profile) {
        try {
            return ResponseEntity.ok(service.createOrUpdate(profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(service.getMyProfile());
    }

    @DeleteMapping
    public ResponseEntity<?> delete() {
        try {
            return ResponseEntity.ok(service.deleteProfile());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PutMapping("/updatePic")
    public ResponseEntity<?> updateProfilePicture(@RequestBody java.util.Map<String, String> requestBody) {
        try {
            String picUrl = requestBody.get("logoUrl");
            return ResponseEntity.ok(service.updateProfilePicture(picUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}