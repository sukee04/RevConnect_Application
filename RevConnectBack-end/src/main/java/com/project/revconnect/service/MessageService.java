package com.project.revconnect.service;

import com.project.revconnect.dto.MessageResponseDTO;
import com.project.revconnect.dto.SharedPostPayloadDTO;
import com.project.revconnect.model.Message;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.MessageRepository;
import com.project.revconnect.repository.UserRepository;
import com.project.revconnect.util.AuthUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository,
                          NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public MessageResponseDTO sendMessage(Long receiverId, String content, SharedPostPayloadDTO sharedPost) {
        User sender = AuthUtil.getLoggedInUser(userRepository);
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message(sender, receiver, content);
        if (sharedPost != null) {
            message.setSharedPostId(sharedPost.getPostId());
            message.setSharedPostMediaUrl(sharedPost.getMediaUrl());
            message.setSharedPostMediaType(sharedPost.getMediaType());
            message.setSharedPostDescription(sharedPost.getDescription());
            message.setSharedPostAuthorUsername(sharedPost.getAuthorUsername());
        }
        Message saved = messageRepository.save(message);
        notificationService.createNotification(receiver, sender, "MESSAGE", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional
    public List<MessageResponseDTO> getConversation(Long otherUserId) {
        User currentUser = AuthUtil.getLoggedInUser(userRepository);
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        messageRepository.markConversationAsRead(currentUser, otherUser);

        return messageRepository.findConversation(currentUser, otherUser)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<User> getMessagePartners() {
        User currentUser = AuthUtil.getLoggedInUser(userRepository);
        return messageRepository.findMessagePartners(currentUser);
    }

    public long getUnreadCount() {
        User currentUser = AuthUtil.getLoggedInUser(userRepository);
        return messageRepository.countByReceiverAndReadFalse(currentUser);
    }

    public Map<Long, Long> getUnreadCountByPartner() {
        User currentUser = AuthUtil.getLoggedInUser(userRepository);
        List<Object[]> rows = messageRepository.countUnreadBySender(currentUser);
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            Long partnerId = row[0] instanceof Number ? ((Number) row[0]).longValue() : null;
            Long unreadCount = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            if (partnerId != null) {
                result.put(partnerId, unreadCount);
            }
        }
        return result;
    }

    private MessageResponseDTO mapToDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getReceiver().getId(),
                message.getReceiver().getUsername(),
                message.getContent(),
                message.getSharedPostId(),
                message.getSharedPostMediaUrl(),
                message.getSharedPostMediaType(),
                message.getSharedPostDescription(),
                message.getSharedPostAuthorUsername(),
                message.getTimestamp());
    }

    public MessageResponseDTO editMessage(Long messageId, String newContent) {
        User currentUser = AuthUtil.getLoggedInUser(userRepository);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized: You can only edit your own messages");
        }

        message.setContent(newContent);
        Message updated = messageRepository.save(message);
        return mapToDTO(updated);
    }

    public void deleteMessage(Long messageId) {
        User currentUser = AuthUtil.getLoggedInUser(userRepository);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized: You can only delete your own messages");
        }

        messageRepository.delete(message);
    }
}
