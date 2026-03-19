package com.project.revconnect.service;

import com.project.revconnect.dto.CommentRequestDTO;
import com.project.revconnect.dto.CommentResponseDTO;
import com.project.revconnect.model.Comment;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.CommentRepository;
import com.project.revconnect.repository.PostRepository;
import com.project.revconnect.repository.UserRepository;
import com.project.revconnect.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<!\\w)@([A-Za-z0-9._]{2,30})");

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                          UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ✅ FIX: Removed ModelMapper — Comment's user/post fields are @JsonIgnore,
    // ModelMapper can't traverse them reliably. Map manually instead.
    private CommentResponseDTO mapToDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setPostId(comment.getPost().getId());
        return dto;
    }

    public CommentResponseDTO addComment(Long postId, CommentRequestDTO request) {
        User user = AuthUtil.getLoggedInUser(userRepository);
        if (user == null) throw new RuntimeException("Not authenticated");

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("Comment content cannot be empty");
        }

        Comment comment = new Comment(request.getContent(), user, post);
        Comment saved = commentRepository.save(comment);

        Set<Long> alreadyNotified = new HashSet<>();
        // Don't notify yourself
        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(post.getUser(), user, "COMMENT", post.getId());
            alreadyNotified.add(post.getUser().getId());
        }

        // Mention notifications from comment body: @username
        for (User mentioned : findMentionedUsers(request.getContent(), user.getId())) {
            if (alreadyNotified.contains(mentioned.getId())) {
                continue;
            }
            notificationService.createNotification(mentioned, user, "COMMENT_MENTION", post.getId());
            alreadyNotified.add(mentioned.getId());
        }

        return mapToDTO(saved);
    }

    public List<CommentResponseDTO> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return commentRepository.findByPost(post).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId) {
        User user = AuthUtil.getLoggedInUser(userRepository);
        if (user == null) throw new RuntimeException("Not authenticated");

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private Set<User> findMentionedUsers(String content, Long actorUserId) {
        Set<User> result = new HashSet<>();
        Set<Long> seenIds = new HashSet<>();
        if (content == null || content.isBlank()) {
            return result;
        }

        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String username = matcher.group(1);
            if (username == null || username.isBlank()) {
                continue;
            }

            User mentioned = userRepository.findByUsername(username.trim());
            if (mentioned == null) {
                continue;
            }
            if (actorUserId != null && actorUserId.equals(mentioned.getId())) {
                continue;
            }
            if (seenIds.contains(mentioned.getId())) {
                continue;
            }
            result.add(mentioned);
            seenIds.add(mentioned.getId());
        }
        return result;
    }
}
