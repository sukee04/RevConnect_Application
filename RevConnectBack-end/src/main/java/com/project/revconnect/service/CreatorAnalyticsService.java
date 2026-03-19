package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.*;
import com.project.revconnect.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CreatorAnalyticsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final SavedPostRepository savedPostRepository;
    private final FollowingRepository followingRepository;
    private final PostViewEventRepository postViewEventRepository;
    private final StoryRepository storyRepository;
    private final StoryViewEventRepository storyViewEventRepository;

    public CreatorAnalyticsService(UserRepository userRepository,
                                   PostRepository postRepository,
                                   LikeRepository likeRepository,
                                   CommentRepository commentRepository,
                                   SavedPostRepository savedPostRepository,
                                   FollowingRepository followingRepository,
                                   PostViewEventRepository postViewEventRepository,
                                   StoryRepository storyRepository,
                                   StoryViewEventRepository storyViewEventRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.savedPostRepository = savedPostRepository;
        this.followingRepository = followingRepository;
        this.postViewEventRepository = postViewEventRepository;
        this.storyRepository = storyRepository;
        this.storyViewEventRepository = storyViewEventRepository;
    }

    private User getCreatorUser() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }
        if (user.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only creator accounts can access creator analytics");
        }
        return user;
    }

    public Map<String, Object> getDashboard() {
        User creator = getCreatorUser();
        List<Post> posts = postRepository.findByUser_IdAndPublishedTrueOrderByCreatedAtDesc(creator.getId());

        List<Map<String, Object>> postMetrics = posts.stream()
                .map(this::buildPostMetrics)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("creatorId", creator.getId());
        response.put("postLevelMetrics", postMetrics);
        response.put("followerDemographics", buildFollowerDemographics(creator));
        response.put("bestTimeToPost", buildBestTimeToPost(posts));
        response.put("followerGrowth", buildFollowerGrowth(creator));
        response.put("storyMetrics", buildStoryMetrics(creator));
        response.put("reelMetrics", buildReelMetrics(posts));
        return response;
    }

    public Map<String, Object> recordPostView(Long postId, Double watchSeconds, Boolean completed) {
        User viewer = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
        if (viewer == null) {
            throw new RuntimeException("Not authenticated");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getUser() != null && post.getUser().getId().equals(viewer.getId())) {
            return Map.of("message", "Own post view ignored");
        }

        PostViewEvent event = new PostViewEvent();
        event.setPost(post);
        event.setViewer(viewer);
        event.setWatchSeconds(normalizeWatchSeconds(watchSeconds));
        event.setCompleted(completed != null && completed);
        postViewEventRepository.save(event);

        return Map.of("message", "Post view tracked");
    }

    public Map<String, Object> recordStoryView(Long storyId, Boolean tapThrough) {
        User viewer = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
        if (viewer == null) {
            throw new RuntimeException("Not authenticated");
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (story.getUser() != null && story.getUser().getId().equals(viewer.getId())) {
            return Map.of("message", "Own story view ignored");
        }

        StoryViewEvent event = new StoryViewEvent();
        event.setStory(story);
        event.setViewer(viewer);
        event.setTapThrough(tapThrough != null && tapThrough);
        storyViewEventRepository.save(event);

        return Map.of("message", "Story view tracked");
    }

    private Map<String, Object> buildPostMetrics(Post post) {
        long impressions = postViewEventRepository.countByPost_Id(post.getId());
        long reach = postViewEventRepository.countDistinctViewersByPostId(post.getId());
        long saves = savedPostRepository.countByPost_Id(post.getId());
        long likes = likeRepository.countByPost(post);
        long comments = commentRepository.countByPost(post);
        double watchTime = postViewEventRepository.sumWatchSecondsByPostId(post.getId());
        long completions = postViewEventRepository.countByPost_IdAndCompletedTrue(post.getId());
        double completionRate = impressions == 0 ? 0.0 : round2(((double) completions * 100.0) / impressions);

        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("postId", post.getId());
        metric.put("createdAt", post.getCreatedAt());
        metric.put("mediaType", post.getMediaType());
        metric.put("reach", reach);
        metric.put("impressions", impressions);
        metric.put("saves", saves);
        metric.put("likes", likes);
        metric.put("comments", comments);
        metric.put("watchTimeSeconds", round2(watchTime));
        metric.put("completionRate", completionRate);
        return metric;
    }

    private Map<String, Object> buildFollowerDemographics(User creator) {
        List<Following> followers = followingRepository.findByFollowingUser(creator);

        Map<String, Long> ageBuckets = new LinkedHashMap<>();
        ageBuckets.put("13-17", 0L);
        ageBuckets.put("18-24", 0L);
        ageBuckets.put("25-34", 0L);
        ageBuckets.put("35-44", 0L);
        ageBuckets.put("45+", 0L);
        ageBuckets.put("Unknown", 0L);

        Map<String, Long> genderCounts = new LinkedHashMap<>();
        Map<String, Long> locationCounts = new LinkedHashMap<>();

        for (Following relation : followers) {
            User follower = relation.getUser();
            UserProfile profile = follower == null ? null : follower.getUserProfile();

            String ageBucket = toAgeBucket(profile == null ? null : profile.getAge());
            ageBuckets.put(ageBucket, ageBuckets.getOrDefault(ageBucket, 0L) + 1);

            String gender = normalizeGender(profile == null ? null : profile.getGender());
            genderCounts.put(gender, genderCounts.getOrDefault(gender, 0L) + 1);

            String location = normalizeLocation(profile == null ? null : profile.getLocation());
            locationCounts.put(location, locationCounts.getOrDefault(location, 0L) + 1);
        }

        List<Map<String, Object>> topLocations = locationCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(entry -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("location", entry.getKey());
                    data.put("count", entry.getValue());
                    return data;
                })
                .toList();

        Map<String, Object> demographics = new LinkedHashMap<>();
        demographics.put("totalFollowers", followers.size());
        demographics.put("age", ageBuckets);
        demographics.put("gender", genderCounts);
        demographics.put("topLocations", topLocations);
        return demographics;
    }

    private Map<String, Object> buildBestTimeToPost(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of(
                    "hour24", -1,
                    "label", "Not enough data yet",
                    "averageEngagement", 0.0,
                    "hourlyBreakdown", List.of());
        }

        Map<Integer, List<Double>> perHourScores = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post.getCreatedAt() == null) {
                continue;
            }
            long likes = likeRepository.countByPost(post);
            long comments = commentRepository.countByPost(post);
            long saves = savedPostRepository.countByPost_Id(post.getId());
            long impressions = postViewEventRepository.countByPost_Id(post.getId());
            double score = likes + comments + saves + (impressions * 0.2);
            perHourScores.computeIfAbsent(post.getCreatedAt().getHour(), key -> new ArrayList<>()).add(score);
        }

        int bestHour = -1;
        double bestAvg = -1;
        List<Map<String, Object>> breakdown = new ArrayList<>();

        for (Map.Entry<Integer, List<Double>> entry : perHourScores.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            if (avg > bestAvg) {
                bestAvg = avg;
                bestHour = entry.getKey();
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("hour24", entry.getKey());
            row.put("averageEngagement", round2(avg));
            row.put("postCount", entry.getValue().size());
            breakdown.add(row);
        }

        String label = bestHour < 0
                ? "Not enough data yet"
                : String.format("%02d:00 - %02d:00", bestHour, (bestHour + 1) % 24);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hour24", bestHour);
        response.put("label", label);
        response.put("averageEngagement", round2(bestAvg < 0 ? 0 : bestAvg));
        response.put("hourlyBreakdown", breakdown);
        return response;
    }

    private Map<String, Object> buildFollowerGrowth(User creator) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        List<Following> windowFollows = followingRepository
                .findByFollowingUserAndCreatedAtAfterOrderByCreatedAtAsc(creator, startDateTime);

        long totalFollowers = followingRepository.countByFollowingUser(creator);
        long baselineFollowers = Math.max(0, totalFollowers - windowFollows.size());

        Map<LocalDate, Long> incrementsByDay = windowFollows.stream()
                .filter(f -> f.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<Map<String, Object>> points = new ArrayList<>();
        long runningTotal = baselineFollowers;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        for (LocalDate day = startDate; !day.isAfter(today); day = day.plusDays(1)) {
            runningTotal += incrementsByDay.getOrDefault(day, 0L);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", formatter.format(day));
            point.put("followers", runningTotal);
            points.add(point);
        }

        long growthInWindow = points.isEmpty() ? 0L
                : ((Number) points.get(points.size() - 1).get("followers")).longValue()
                - ((Number) points.get(0).get("followers")).longValue();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("windowDays", 30);
        response.put("growth", growthInWindow);
        response.put("points", points);
        return response;
    }

    private Map<String, Object> buildStoryMetrics(User creator) {
        long storyViews = storyViewEventRepository.countByStory_User_Id(creator.getId());
        long storyTapThroughs = storyViewEventRepository.countByStory_User_IdAndTapThroughTrue(creator.getId());
        double tapThroughRate = storyViews == 0 ? 0.0 : round2(((double) storyTapThroughs * 100.0) / storyViews);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("views", storyViews);
        response.put("tapThroughRate", tapThroughRate);
        return response;
    }

    private Map<String, Object> buildReelMetrics(List<Post> posts) {
        List<Post> reelPosts = posts.stream()
                .filter(p -> p.getMediaType() != null && "VIDEO".equalsIgnoreCase(p.getMediaType()))
                .toList();

        long totalViews = 0;
        long completionEvents = 0;
        double watchSeconds = 0.0;

        for (Post reel : reelPosts) {
            totalViews += postViewEventRepository.countByPost_Id(reel.getId());
            completionEvents += postViewEventRepository.countByPost_IdAndCompletedTrue(reel.getId());
            watchSeconds += postViewEventRepository.sumWatchSecondsByPostId(reel.getId());
        }

        double completionRate = totalViews == 0 ? 0.0 : round2(((double) completionEvents * 100.0) / totalViews);
        double averageWatchTime = totalViews == 0 ? 0.0 : round2(watchSeconds / totalViews);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalReels", reelPosts.size());
        response.put("watchTimeSeconds", round2(watchSeconds));
        response.put("averageWatchTimeSeconds", averageWatchTime);
        response.put("completionRate", completionRate);
        return response;
    }

    private String toAgeBucket(Integer age) {
        if (age == null || age < 13) return "Unknown";
        if (age <= 17) return "13-17";
        if (age <= 24) return "18-24";
        if (age <= 34) return "25-34";
        if (age <= 44) return "35-44";
        return "45+";
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) return "Unknown";
        String normalized = gender.trim();
        if (normalized.length() > 24) {
            normalized = normalized.substring(0, 24);
        }
        return normalized;
    }

    private String normalizeLocation(String location) {
        if (location == null || location.isBlank()) return "Unknown";
        String normalized = location.trim();
        if (normalized.length() > 80) {
            normalized = normalized.substring(0, 80);
        }
        return normalized;
    }

    private double normalizeWatchSeconds(Double watchSeconds) {
        if (watchSeconds == null || watchSeconds < 0) return 0.0;
        if (watchSeconds > 7200) return 7200;
        return watchSeconds;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
