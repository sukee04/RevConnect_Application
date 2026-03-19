package com.project.revconnect.controller;

import com.project.revconnect.dto.MessageResponseDTO;
import com.project.revconnect.dto.SendMessageRequestDTO;
import com.project.revconnect.dto.SharedPostPayloadDTO;
import com.project.revconnect.model.User;
import com.project.revconnect.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/messages", "/messages"})
public class MessageController {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/partners")
    public ResponseEntity<List<Map<String, Object>>> getMessagePartners() {
        List<User> partners = messageService.getMessagePartners();
        Map<Long, Long> unreadByPartner = messageService.getUnreadCountByPartner();
        List<Map<String, Object>> response = partners.stream().map(u -> {
            String pic = null;
            if (u.getUserProfile() != null) pic = u.getUserProfile().getProfilepicURL();
            else if (u.getCreatorProfile() != null) pic = u.getCreatorProfile().getProfilepicURL();
            else if (u.getBusinessProfile() != null) pic = u.getBusinessProfile().getLogoUrl();
            final String finalPic = pic;
            long unreadCount = unreadByPartner.getOrDefault(u.getId(), 0L);
            return Map.<String, Object>of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "profilePicUrl", finalPic != null ? finalPic : "",
                    "unreadCount", unreadCount);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/unread/count", "/unread-count"})
    public ResponseEntity<Long> getUnreadMessageCount() {
        return ResponseEntity.ok(messageService.getUnreadCount());
    }

    @GetMapping({"/conversation/{userId}", "/chat/{userId}"})
    public ResponseEntity<List<MessageResponseDTO>> getConversation(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getConversation(userId));
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long receiverId,
                                         @RequestBody SendMessageRequestDTO payload) {
        if (payload == null) {
            return ResponseEntity.badRequest().body("Invalid message payload");
        }

        String rawContent = payload.getContent();
        String content = rawContent == null ? "" : rawContent.trim();
        SharedPostPayloadDTO sharedPost = payload.getSharedPost();
        boolean hasSharedPost = sharedPost != null && sharedPost.getPostId() != null;

        if (content.isEmpty() && !hasSharedPost) {
            return ResponseEntity.badRequest().body("Message content cannot be empty");
        }

        if (content.length() > MAX_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest().body("Message content must be 1000 characters or less");
        }

        if (hasSharedPost) {
            if (sharedPost.getAuthorUsername() != null) {
                sharedPost.setAuthorUsername(sharedPost.getAuthorUsername().trim());
            }
            if (sharedPost.getDescription() != null && sharedPost.getDescription().length() > 2000) {
                sharedPost.setDescription(sharedPost.getDescription().substring(0, 2000));
            }
        }

        return ResponseEntity.ok(messageService.sendMessage(receiverId, content, hasSharedPost ? sharedPost : null));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<?> editMessage(@PathVariable Long messageId,
                                         @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message content cannot be empty");
        }
        content = content.trim();
        if (content.length() > MAX_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest().body("Message content must be 1000 characters or less");
        }
        return ResponseEntity.ok(messageService.editMessage(messageId, content));
    }

    @DeleteMapping({"/{messageId}", "/delete/{messageId}"})
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok("Message deleted");
    }
}
