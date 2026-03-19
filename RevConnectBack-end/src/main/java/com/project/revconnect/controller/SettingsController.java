package com.project.revconnect.controller;

import com.project.revconnect.dto.AccountActionRequestDTO;
import com.project.revconnect.dto.AccountDetailsUpdateDTO;
import com.project.revconnect.dto.PasswordUpdateDTO;
import com.project.revconnect.model.User;
import com.project.revconnect.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateDTO request) {
        if (request.getOldPassword() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body("Old and new passwords are required");
        }
        if (request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body("New password must be at least 8 characters");
        }
        String result = userService.updatePassword(request.getOldPassword(), request.getNewPassword());
        return result.equals("Password updated successfully")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/account")
    public ResponseEntity<?> updateAccountDetails(@RequestBody AccountDetailsUpdateDTO request) {
        String result = userService.updateAccountDetails(request.getUsername(), request.getEmail());
        return switch (result) {
            case "Account details updated successfully" -> {
                User user = userService.getCurrentUser();
                String refreshedToken = userService.generateTokenForCurrentUser();

                Map<String, Object> payload = new HashMap<>();
                payload.put("message", "Account details updated successfully");
                payload.put("user", user);
                if (refreshedToken != null) {
                    payload.put("token", refreshedToken);
                }

                yield ResponseEntity.ok(payload);
            }
            case "USERNAME_EXISTS" -> ResponseEntity.badRequest().body("Username already taken");
            case "EMAIL_EXISTS"    -> ResponseEntity.badRequest().body("Email already registered");
            case "USER_NOT_FOUND"  -> ResponseEntity.status(404).body("User not found");
            default                -> ResponseEntity.badRequest().body(result);
        };
    }

    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(@RequestBody AccountActionRequestDTO request) {
        String result = userService.deactivateAccount(request != null ? request.getPassword() : null);
        return result.equals("Account deactivated successfully")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/delete")
    public ResponseEntity<String> deleteAccount(@RequestBody AccountActionRequestDTO request) {
        String result = userService.deleteCurrentUser(request != null ? request.getPassword() : null);
        return result.equals("Account deleted successfully")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}
