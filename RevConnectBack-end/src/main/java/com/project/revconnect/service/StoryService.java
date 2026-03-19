package com.project.revconnect.service;

import com.project.revconnect.model.Story;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.CreatorSubscriptionRepository;
import com.project.revconnect.repository.FollowingRepository;
import com.project.revconnect.repository.StoryRepository;
import com.project.revconnect.repository.StoryViewEventRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final FollowingRepository followingRepository;
    private final CreatorSubscriptionRepository creatorSubscriptionRepository;
    private final StoryViewEventRepository storyViewEventRepository;

    public StoryService(StoryRepository storyRepository, UserRepository userRepository,
                        FollowingRepository followingRepository,
                        CreatorSubscriptionRepository creatorSubscriptionRepository,
                        StoryViewEventRepository storyViewEventRepository) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.followingRepository = followingRepository;
        this.creatorSubscriptionRepository = creatorSubscriptionRepository;
        this.storyViewEventRepository = storyViewEventRepository;
    }

    private User getLoggedInUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    public Story createStory(Story story) {
        User user = getLoggedInUser();
        if (story.isSubscriberOnly() && user.getRole() != com.project.revconnect.enums.Handlers.CREATER) {
            throw new RuntimeException("Subscriber-only stories are available for creator accounts only.");
        }

        String mediaType = story.getMediaType() == null ? "IMAGE" : story.getMediaType().trim().toUpperCase();
        if (!"IMAGE".equals(mediaType) && !"VIDEO".equals(mediaType)) {
            throw new RuntimeException("Unsupported story media type. Allowed: IMAGE or VIDEO.");
        }

        story.setMediaType(mediaType);
        story.setUser(user);
        return storyRepository.save(story);
    }

    public List<Story> getMyStories() {
        User user = getLoggedInUser();

        return storyRepository.findByUser_IdAndActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(
                user.getId(), LocalDateTime.now());
    }

    public List<Story> getFeedStories() {
        User user = getLoggedInUser();
        List<Story> allFeedStories = storyRepository.findFeedStories(user.getId(), LocalDateTime.now());

        Set<Long> followedIds = followingRepository.findByUser(user)
                .stream().map(f -> f.getFollowingUser().getId())
                .collect(Collectors.toSet());

        return allFeedStories.stream().filter(story -> {
            User author = story.getUser();
            if (author.getId().equals(user.getId())) return true;

            if (story.isSubscriberOnly()) {
                return creatorSubscriptionRepository.existsByCreator_IdAndSubscriber_IdAndStatus(
                        author.getId(), user.getId(), "ACTIVE");
            }

            boolean isPublic = author.getUserProfile() == null || author.getUserProfile().isPublic();
            if (isPublic) return true;
            return followedIds.contains(author.getId());
        }).collect(Collectors.toList());
    }

    public String deleteStory(Long storyId) {
        User user = getLoggedInUser();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        story.setActive(false);
        storyRepository.save(story);
        return "Story deleted";
    }

    public List<Map<String, Object>> getStoryViewers(Long storyId) {
        User user = getLoggedInUser();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (story.getUser() == null || !story.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<com.project.revconnect.model.StoryViewEvent> events =
                storyViewEventRepository.findByStory_IdOrderByViewedAtDesc(storyId);
        Map<Long, Map<String, Object>> unique = new LinkedHashMap<>();
        for (com.project.revconnect.model.StoryViewEvent event : events) {
            User viewer = event.getViewer();
            if (viewer == null || unique.containsKey(viewer.getId())) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", viewer.getId());
            entry.put("username", viewer.getUsername());
            String avatar = null;
            if (viewer.getCreatorProfile() != null) {
                avatar = viewer.getCreatorProfile().getProfilepicURL();
            } else if (viewer.getBusinessProfile() != null) {
                avatar = viewer.getBusinessProfile().getLogoUrl();
            } else if (viewer.getUserProfile() != null) {
                avatar = viewer.getUserProfile().getProfilepicURL();
            }
            entry.put("avatarUrl", avatar);
            entry.put("viewedAt", event.getViewedAt());
            unique.put(viewer.getId(), entry);
        }
        return new ArrayList<>(unique.values());
    }
}

