package com.project.revconnect.service;

import com.project.revconnect.model.Like;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.LikeRepository;
import com.project.revconnect.repository.PostRepository;
import com.project.revconnect.repository.UserRepository;
import com.project.revconnect.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public LikeService(LikeRepository likeRepository, PostRepository postRepository, UserRepository userRepository,
            NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void toggleLike(Long postId) {

        User user = AuthUtil.getLoggedInUser(userRepository);
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            Like like = new Like(user, post);
            likeRepository.save(like);


            notificationService.createNotification(post.getUser(), user, "LIKE", post.getId());
        }
    }

    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return likeRepository.countByPost(post);
    }

    public boolean isLikedByUser(Long postId) {

        User user = AuthUtil.getLoggedInUser(userRepository);
        if (user == null) {
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return likeRepository.existsByUserAndPost(user, post);
    }

    public List<Map<String, Object>> getLikedUsers(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return likeRepository.findByPostOrderByIdDesc(post)
                .stream()
                .map(Like::getUser)
                .distinct()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "avatarUrl", resolveAvatar(user)
                ))
                .collect(Collectors.toList());
    }

    private String resolveAvatar(User user) {
        if (user == null) {
            return "";
        }

        if (user.getUserProfile() != null && user.getUserProfile().getProfilepicURL() != null) {
            return user.getUserProfile().getProfilepicURL();
        }
        if (user.getCreatorProfile() != null && user.getCreatorProfile().getProfilepicURL() != null) {
            return user.getCreatorProfile().getProfilepicURL();
        }
        if (user.getBusinessProfile() != null && user.getBusinessProfile().getLogoUrl() != null) {
            return user.getBusinessProfile().getLogoUrl();
        }
        return "";
    }
}
