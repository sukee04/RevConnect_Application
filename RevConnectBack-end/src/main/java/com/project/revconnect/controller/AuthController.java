package com.project.revconnect.controller;

import com.project.revconnect.model.User;
import com.project.revconnect.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        return ResponseEntity.ok("Logged out successfully");
    }


    @GetMapping("/auth/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("query") String query) {
        return ResponseEntity.ok(
                userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query));
    }
}
