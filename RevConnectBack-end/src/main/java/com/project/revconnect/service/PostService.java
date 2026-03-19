package com.project.revconnect.service;

import com.project.revconnect.dto.PostResponseDTO;
import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.PostTag;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.*;
import com.project.revconnect.service.NotificationService;
import com.project.revconnect.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<!\\w)@([A-Za-z0-9._]{2,30})");

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowingRepository followingRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final SavedPostRepository savedPostRepository;
    private final PostTagRepository postTagRepository;
    private final PostViewEventRepository postViewEventRepository;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       FollowingRepository followingRepository,
                       LikeRepository likeRepository,
                       CommentRepository commentRepository,
                       SavedPostRepository savedPostRepository,
                       PostTagRepository postTagRepository,
                       PostViewEventRepository postViewEventRepository,
                       NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.followingRepository = followingRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.savedPostRepository = savedPostRepository;
        this.postTagRepository = postTagRepository;
        this.postViewEventRepository = postViewEventRepository;
        this.notificationService = notificationService;
    }

    private PostResponseDTO mapToDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setPostId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setMediaUrl(post.getMediaUrl());
        dto.setMediaType(post.getMediaType());
        dto.setProductLink(post.getProductLink());
        dto.setScheduledAt(post.getScheduledAt());
        dto.setPinned(post.isPinned());
        dto.setPublished(post.isPublished());
        dto.setCollabAccepted(post.isCollabAccepted());
        dto.setSeriesName(post.getSeriesName());
        dto.setSeriesOrder(post.getSeriesOrder());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(likeRepository.countByPost(post));
        dto.setCommentCount(commentRepository.countByPost(post));
        dto.setSaveCount(savedPostRepository.countByPost_Id(post.getId()));

        if (post.getUser() != null) {
            User u = post.getUser();
            dto.setUserId(u.getId());
            dto.setUserName(u.getUsername());


            String pic = null;
            if (u.getRole() == Handlers.Business_Account_User && u.getBusinessProfile() != null) {
                pic = u.getBusinessProfile().getLogoUrl();
            } else if (u.getRole() == Handlers.CREATER && u.getCreatorProfile() != null) {
                pic = u.getCreatorProfile().getProfilepicURL();
            } else if (u.getUserProfile() != null) {
                pic = u.getUserProfile().getProfilepicURL();
            }
            dto.setAuthorProfilePicture(pic);
        }

        if (post.getCollaborator() != null) {
            dto.setCollaboratorId(post.getCollaborator().getId());
            dto.setCollaboratorUsername(post.getCollaborator().getUsername());
        }
        return dto;
    }

    public ResponseEntity<PostResponseDTO> addPost(Post post) {
        User user = AuthUtil.getLoggedInUser(userRepository);
        if (user == null) return ResponseEntity.status(401).build();

        resolveCollaborator(post, user);
        normalizeSeriesFields(post);
        post.setProductLink(trimToLength(post.getProductLink(), 1200));
        applyScheduleState(post, user);
        post.setUser(user);
        Post savedPost = postRepository.save(post);
        if (savedPost.getCollaborator() != null && !savedPost.isCollabAccepted()) {
            notificationService.createNotification(
                    savedPost.getCollaborator(),
                    user,
                    "COLLAB_POST_REQUEST",
                    savedPost.getId()
            );
        }
        Set<User> taggedUsers = resolveTaggedUsers(post.getTaggedUsernames(), post.getDescription(), user.getId());
        syncPostTags(savedPost, taggedUsers, user);
        triggerPostMentionNotifications(savedPost, user);
        return ResponseEntity.ok(mapToDTO(savedPost));
    }

    public ResponseEntity<List<PostResponseDTO>> findMyPosts() {
        User user = AuthUtil.getLoggedInUser(userRepository);
        if (user == null) return ResponseEntity.status(401).build();
        List<Post> ownPosts = postRepository.findByUser_IdOrderByIsPinnedDescCreatedAtDesc(user.getId());
        List<Post> collabPosts = postRepository.findByCollaborator_IdAndCollabAcceptedTrueOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(
                mergeAndSortPosts(ownPosts, collabPosts)
                        .stream()
                        .map(this::mapToDTO)
                        .toList());
    }

    public ResponseEntity<List<PostResponseDTO>> findPostsByUsername(String username) {
        User targetUser = userRepository.findByUsername(username);
        if (targetUser == null) return ResponseEntity.notFound().build();

        User loggedInUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedInUser == null) return ResponseEntity.status(401).build();


        if (targetUser.getRole() == Handlers.USER) {
            boolean isPublic = targetUser.getUserProfile() == null || targetUser.getUserProfile().isPublic();
            boolean isOwn = targetUser.getId().equals(loggedInUser.getId());
            boolean isFollowing = followingRepository.findByUserAndFollowingUser(loggedInUser, targetUser) != null;

            if (!isPublic && !isOwn && !isFollowing) {
                return ResponseEntity.ok(Collections.emptyList());
            }
        }

        List<Post> ownPosts = postRepository.findByUser_IdOrderByIsPinnedDescCreatedAtDesc(targetUser.getId());
        List<Post> collabPosts = postRepository.findByCollaborator_IdAndCollabAcceptedTrueOrderByCreatedAtDesc(targetUser.getId());

        return ResponseEntity.ok(
                mergeAndSortPosts(ownPosts, collabPosts).stream()
                        .filter(post -> isPostVisibleToViewer(post, loggedInUser, targetUser))
                        .map(this::mapToDTO)
                        .toList());
    }

    public ResponseEntity<List<PostResponseDTO>> findTaggedPostsByUsername(String username) {
        User taggedUser = userRepository.findByUsername(username);
        if (taggedUser == null) return ResponseEntity.notFound().build();

        User loggedInUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedInUser == null) return ResponseEntity.status(401).build();

        List<Post> relationTaggedPosts = postTagRepository.findTaggedPostsByUsername(username);

        return ResponseEntity.ok(
                relationTaggedPosts.stream()
                        .filter(post -> post.getUser() != null)
                        .filter(post -> isPostVisibleToViewer(post, loggedInUser, post.getUser()))
                        .map(this::mapToDTO)
                        .toList());
    }

    public ResponseEntity<PostResponseDTO> updatePost(Long postId, Post post) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post existing = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!existing.getUser().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (post.getDescription() != null) existing.setDescription(post.getDescription());
        if (post.getMediaUrl() != null) existing.setMediaUrl(post.getMediaUrl());
        if (post.getMediaType() != null) existing.setMediaType(post.getMediaType());
        if (post.getProductLink() != null) existing.setProductLink(trimToLength(post.getProductLink(), 1200));
        if (post.getHashtags() != null) existing.setHashtags(post.getHashtags());
        if (post.getSeriesName() != null) existing.setSeriesName(post.getSeriesName());
        if (post.getSeriesOrder() != null) existing.setSeriesOrder(post.getSeriesOrder());
        normalizeSeriesFields(existing);

        if (post.getCollaborator() != null || post.getCollaboratorId() != null || post.getCollaboratorUsername() != null) {
            resolveCollaborator(post, loggedUser);
            existing.setCollaborator(post.getCollaborator());
            existing.setCollabAccepted(post.isCollabAccepted());
        }

        if (post.getScheduledAt() != null || post.isPublished() != existing.isPublished()) {
            existing.setScheduledAt(post.getScheduledAt());
            applyScheduleState(existing, loggedUser);
        }

        Post updatedPost = postRepository.save(existing);
        Set<User> taggedUsers = resolveTaggedUsers(post.getTaggedUsernames(), existing.getDescription(), loggedUser.getId());
        syncPostTags(updatedPost, taggedUsers, loggedUser);
        triggerPostMentionNotifications(updatedPost, loggedUser);
        return ResponseEntity.ok(mapToDTO(updatedPost));
    }

    @Transactional
    public ResponseEntity<PostResponseDTO> deletePost(Long postId) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        PostResponseDTO response = mapToDTO(post);
        post.setCollaborator(null);
        post.setCollabAccepted(true);
        postRepository.save(post);
        commentRepository.deleteByPost_Id(postId);
        likeRepository.deleteByPost_Id(postId);
        savedPostRepository.deleteByPost_Id(postId);
        postTagRepository.deleteByPostId(postId);
        postViewEventRepository.deleteByPost_Id(postId);
        postRepository.delete(post);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<PostResponseDTO>> deleteAllPost() {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        List<Post> posts = postRepository.findByUser_Id(loggedUser.getId());
        List<PostResponseDTO> response = posts.stream().map(this::mapToDTO).toList();
        commentRepository.deleteByPost_User_Id(loggedUser.getId());
        likeRepository.deleteByPost_User_Id(loggedUser.getId());
        savedPostRepository.deleteByPost_User_Id(loggedUser.getId());
        postTagRepository.deleteByPostUserId(loggedUser.getId());
        postViewEventRepository.deleteByPost_User_Id(loggedUser.getId());
        postRepository.deleteAll(posts);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<PostResponseDTO> pinPost(Long postId) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        if (loggedUser.getRole() != Handlers.CREATER) {
            return ResponseEntity.badRequest().build();
        }

        if (!post.isPinned() && postRepository.countByUser_IdAndIsPinnedTrue(loggedUser.getId()) >= 3) {
            return ResponseEntity.badRequest().build();
        }

        post.setPinned(true);
        return ResponseEntity.ok(mapToDTO(postRepository.save(post)));
    }

    public ResponseEntity<PostResponseDTO> unpinPost(Long postId) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        post.setPinned(false);
        return ResponseEntity.ok(mapToDTO(postRepository.save(post)));
    }

    public ResponseEntity<List<PostResponseDTO>> getPendingCollabPosts() {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();
        if (loggedUser.getRole() != Handlers.CREATER) return ResponseEntity.badRequest().build();

        List<Post> pending = postRepository.findByCollaborator_IdAndCollabAcceptedFalseOrderByCreatedAtDesc(loggedUser.getId());
        return ResponseEntity.ok(pending.stream().map(this::mapToDTO).toList());
    }

    public ResponseEntity<PostResponseDTO> acceptCollabPost(Long postId) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getCollaborator() == null || !post.getCollaborator().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        post.setCollabAccepted(true);
        return ResponseEntity.ok(mapToDTO(postRepository.save(post)));
    }

    public ResponseEntity<PostResponseDTO> rejectCollabPost(Long postId) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getCollaborator() == null || !post.getCollaborator().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        post.setCollaborator(null);
        post.setCollabAccepted(true);
        return ResponseEntity.ok(mapToDTO(postRepository.save(post)));
    }

    public ResponseEntity<PostResponseDTO> removeSelfFromCollab(Long postId) {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getCollaborator() == null || !post.getCollaborator().getId().equals(loggedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        post.setCollaborator(null);
        post.setCollabAccepted(true);
        return ResponseEntity.ok(mapToDTO(postRepository.save(post)));
    }

    public ResponseEntity<Map<String, List<PostResponseDTO>>> getMyVideoSeries() {
        User loggedUser = AuthUtil.getLoggedInUser(userRepository);
        if (loggedUser == null) return ResponseEntity.status(401).build();
        if (loggedUser.getRole() != Handlers.CREATER) return ResponseEntity.badRequest().build();

        List<Post> seriesPosts = postRepository
                .findByUser_IdAndMediaTypeIgnoreCaseAndSeriesNameIsNotNullOrderBySeriesNameAscSeriesOrderAscCreatedAtDesc(
                        loggedUser.getId(),
                        "VIDEO");

        Map<String, List<PostResponseDTO>> grouped = new LinkedHashMap<>();
        for (Post post : seriesPosts) {
            String seriesName = post.getSeriesName() == null ? "" : post.getSeriesName().trim();
            if (seriesName.isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(seriesName, key -> new ArrayList<>()).add(mapToDTO(post));
        }

        return ResponseEntity.ok(grouped);
    }

    private List<Post> mergeAndSortPosts(List<Post> ownPosts, List<Post> collabPosts) {
        Map<Long, Post> uniquePosts = new LinkedHashMap<>();
        ownPosts.forEach(post -> uniquePosts.put(post.getId(), post));
        collabPosts.forEach(post -> uniquePosts.put(post.getId(), post));

        return uniquePosts.values().stream()
                .sorted(Comparator
                        .comparing(Post::isPinned).reversed()
                        .thenComparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private void resolveCollaborator(Post post, User author) {
        Long collaboratorId = post.getCollaboratorId();
        String collaboratorUsername = post.getCollaboratorUsername();
        User collaboratorFromBody = post.getCollaborator();

        if (collaboratorId == null && collaboratorFromBody != null) {
            collaboratorId = collaboratorFromBody.getId();
        }
        if ((collaboratorId == null || collaboratorId <= 0)
                && (collaboratorUsername == null || collaboratorUsername.isBlank())) {
            post.setCollaborator(null);
            post.setCollabAccepted(true);
            return;
        }

        User collaborator;
        if (collaboratorId != null && collaboratorId > 0) {
            collaborator = userRepository.findById(collaboratorId)
                    .orElseThrow(() -> new RuntimeException("Collaborator user not found"));
        } else {
            collaborator = userRepository.findByUsername(collaboratorUsername.trim());
            if (collaborator == null) {
                throw new RuntimeException("Collaborator user not found");
            }
        }

        if (collaborator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Co-author must be a creator account.");
        }
        if (author.getId().equals(collaborator.getId())) {
            throw new RuntimeException("You cannot set yourself as a collaborator.");
        }

        post.setCollaborator(collaborator);
        post.setCollabAccepted(false);
    }

    private void normalizeSeriesFields(Post post) {
        if (post.getSeriesName() == null) {
            return;
        }
        String trimmed = post.getSeriesName().trim();
        if (trimmed.isBlank()) {
            post.setSeriesName(null);
            post.setSeriesOrder(null);
            return;
        }
        post.setSeriesName(trimmed.length() > 140 ? trimmed.substring(0, 140) : trimmed);
        if (post.getSeriesOrder() != null && post.getSeriesOrder() < 0) {
            post.setSeriesOrder(0);
        }
    }

    private boolean isPostVisibleToViewer(Post post, User viewer, User author) {
        if (author.getId().equals(viewer.getId())) {
            return true;
        }
        return post.isPublished() || (post.getScheduledAt() != null && !post.getScheduledAt().isAfter(LocalDateTime.now()));
    }

    private void applyScheduleState(Post post, User author) {
        LocalDateTime scheduledAt = post.getScheduledAt();
        if (scheduledAt == null) {
            post.setPublished(true);
            return;
        }

        if (author.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Scheduling is available for creator accounts only.");
        }

        post.setPublished(!scheduledAt.isAfter(LocalDateTime.now()));
    }

    private void triggerPostMentionNotifications(Post post, User actor) {
        if (post == null || actor == null) {
            return;
        }

        Set<User> mentionedUsers = findMentionedUsers(post.getDescription(), actor.getId());
        if (mentionedUsers.isEmpty()) {
            return;
        }

        Set<Long> notified = new HashSet<>();
        for (User mentioned : mentionedUsers) {
            if (mentioned.getId().equals(actor.getId())) {
                continue;
            }
            if (notified.contains(mentioned.getId())) {
                continue;
            }
            notificationService.createNotification(mentioned, actor, "POST_MENTION", post.getId());
            notified.add(mentioned.getId());
        }
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

    private String trimToLength(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > maxLen) {
            return trimmed.substring(0, maxLen);
        }
        return trimmed;
    }

    private Set<User> resolveTaggedUsers(List<String> taggedUsernames, String content, Long actorUserId) {
        Set<User> result = new LinkedHashSet<>();
        Set<Long> seenIds = new HashSet<>();

        if (taggedUsernames != null) {
            for (String raw : taggedUsernames) {
                if (raw == null) {
                    continue;
                }
                String normalized = raw.trim().replaceAll("^@+", "");
                if (normalized.isBlank()) {
                    continue;
                }
                User tagged = userRepository.findByUsername(normalized);
                if (tagged == null) {
                    continue;
                }
                if (actorUserId != null && actorUserId.equals(tagged.getId())) {
                    continue;
                }
                if (!seenIds.add(tagged.getId())) {
                    continue;
                }
                result.add(tagged);
            }
            return result;
        }

        return findMentionedUsers(content, actorUserId);
    }

    private void syncPostTags(Post post, Set<User> taggedUsers, User actor) {
        if (post == null || post.getId() == null) {
            return;
        }

        postTagRepository.deleteByPostId(post.getId());
        if (taggedUsers == null || taggedUsers.isEmpty()) {
            return;
        }

        for (User taggedUser : taggedUsers) {
            PostTag tag = new PostTag();
            tag.setPost(post);
            tag.setTaggedUser(taggedUser);
            tag.setTaggedBy(actor);
            postTagRepository.save(tag);
        }
    }

}
