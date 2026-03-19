package com.project.revconnect.service;

import com.project.revconnect.dto.PostResponseDTO;
import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowingRepository followingRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final SavedPostRepository savedPostRepository;

    public FeedService(PostRepository postRepository, UserRepository userRepository,
            FollowingRepository followingRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            SavedPostRepository savedPostRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.followingRepository = followingRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.savedPostRepository = savedPostRepository;
    }

    private User getLoggedInUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    private PostResponseDTO mapToDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setPostId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setUserId(post.getUser().getId());
        dto.setUserName(post.getUser().getUsername());
        dto.setMediaType(post.getMediaType());
        dto.setProductLink(post.getProductLink());
        dto.setScheduledAt(post.getScheduledAt());
        dto.setPinned(post.isPinned());
        dto.setPublished(post.isPublished());

        User user = post.getUser();
        String profilePic = null;

        if (user.getRole() == com.project.revconnect.enums.Handlers.Business_Account_User
                && user.getBusinessProfile() != null) {
            profilePic = user.getBusinessProfile().getLogoUrl();
        } else if (user.getRole() == com.project.revconnect.enums.Handlers.CREATER
                && user.getCreatorProfile() != null) {
            profilePic = user.getCreatorProfile().getProfilepicURL();
        } else if (user.getUserProfile() != null) {
            profilePic = user.getUserProfile().getProfilepicURL();
        }

        dto.setAuthorProfilePicture(profilePic);
        dto.setMediaUrl(post.getMediaUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(likeRepository.countByPost(post));
        dto.setCommentCount(commentRepository.countByPost(post));
        dto.setSaveCount(savedPostRepository.countByPost_Id(post.getId()));
        return dto;
    }

    // 1. Home Feed (Posts from people you follow)
    public List<PostResponseDTO> getHomeFeed() {
        List<Post> allPosts = postRepository.findAll();
        User loggedInUser = getLoggedInUser();

        Set<Long> followedIds = followingRepository.findByUser(loggedInUser)
                .stream().map(f -> f.getFollowingUser().getId())
                .collect(Collectors.toSet());

        List<Post> visiblePosts = allPosts.stream()
                .filter(this::isPublishedOrDue)
                .filter(post -> isAuthorVisibleInHomeFeed(post, loggedInUser, followedIds))
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) {
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        List<Post> businessPosts = visiblePosts.stream()
                .filter(post -> post.getUser() != null && post.getUser().getRole() == Handlers.Business_Account_User)
                .collect(Collectors.toList());
        List<Post> mixedPosts = visiblePosts.stream()
                .filter(post -> post.getUser() != null && post.getUser().getRole() != Handlers.Business_Account_User)
                .collect(Collectors.toList());

        List<Post> ordered = new ArrayList<>();
        int mixedIndex = 0;
        int businessIndex = 0;
        int mixedBatchCount = 0;

        while (mixedIndex < mixedPosts.size() || businessIndex < businessPosts.size()) {
            if (mixedIndex < mixedPosts.size() && mixedBatchCount < 3) {
                ordered.add(mixedPosts.get(mixedIndex++));
                mixedBatchCount++;
                continue;
            }

            if (businessIndex < businessPosts.size()) {
                ordered.add(businessPosts.get(businessIndex++));
                mixedBatchCount = 0;
                continue;
            }

            if (mixedIndex < mixedPosts.size()) {
                ordered.add(mixedPosts.get(mixedIndex++));
                mixedBatchCount++;
            }
        }

        return ordered.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 2. Explore Feed (Posts from Business and Creator profiles)
    public List<PostResponseDTO> getExploreFeed() {
        List<Post> allPosts = postRepository.findAll();

        return allPosts.stream()
                .filter(post -> {
                    if (!isPublishedOrDue(post)) return false;
                    User author = post.getUser();
                    if (author.getRole() == com.project.revconnect.enums.Handlers.Business_Account_User
                            || author.getRole() == com.project.revconnect.enums.Handlers.CREATER) {
                        return true;
                    }

                    // Normal users appear in explore only when public
                    return author.getRole() == com.project.revconnect.enums.Handlers.USER
                            && (author.getUserProfile() == null || author.getUserProfile().isPublic());
                })
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) {
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    }
                    return 0;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private boolean isPublishedOrDue(Post post) {
        return post.isPublished() || (post.getScheduledAt() != null && !post.getScheduledAt().isAfter(LocalDateTime.now()));
    }

    private boolean isAuthorVisibleInHomeFeed(Post post, User loggedInUser, Set<Long> followedIds) {
        User author = post.getUser();
        if (author == null) {
            return false;
        }

        if (author.getId().equals(loggedInUser.getId())) {
            return true;
        }

        Handlers role = author.getRole();

        if (role == Handlers.USER) {
            return followedIds.contains(author.getId());
        }

        if (role == Handlers.CREATER) {
            return true;
        }

        if (role == Handlers.Business_Account_User) {
            boolean isPublic = author.getBusinessProfile() == null || author.getBusinessProfile().isPublic();
            return isPublic || followedIds.contains(author.getId());
        }

        return false;
    }
}
