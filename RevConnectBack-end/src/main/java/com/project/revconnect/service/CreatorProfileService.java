package com.project.revconnect.service;

import com.project.revconnect.dto.CreatorPostDTO;
import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.CreatorProfile;
import com.project.revconnect.model.Post;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.CreatorProfileRepository;
import com.project.revconnect.repository.FollowingRepository;
import com.project.revconnect.repository.PostRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreatorProfileService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowingRepository followingRepository;

    public CreatorProfileService(CreatorProfileRepository creatorProfileRepository,
            UserRepository userRepository,
            PostRepository postRepository,
            FollowingRepository followingRepository) {
        this.creatorProfileRepository = creatorProfileRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followingRepository = followingRepository;
    }

    public CreatorProfile saveCreatorProfile(CreatorProfile profile) {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null)
            throw new RuntimeException("User not found");

        if (user.getRole() != null && user.getRole() != Handlers.CREATER)
            throw new RuntimeException("Only CREATER role users can have a creator profile");

        CreatorProfile existing = creatorProfileRepository.findByUser(user);
        if (existing != null) {
            existing.setDisplayName(profile.getDisplayName());
            existing.setBio(profile.getBio());
            existing.setNiche(profile.getNiche());
            existing.setCreatorCategoryLabel(profile.getCreatorCategoryLabel());
            existing.setProfileGridLayout(
                    profile.getProfileGridLayout() == null || profile.getProfileGridLayout().isBlank()
                            ? "CLASSIC"
                            : profile.getProfileGridLayout().trim().toUpperCase());
            existing.setLinkInBioLinks(sanitizeLinks(profile.getLinkInBioLinks()));
            return creatorProfileRepository.save(existing);
        }

        if (profile.getProfileGridLayout() == null || profile.getProfileGridLayout().isBlank()) {
            profile.setProfileGridLayout("CLASSIC");
        } else {
            profile.setProfileGridLayout(profile.getProfileGridLayout().trim().toUpperCase());
        }
        profile.setLinkInBioLinks(sanitizeLinks(profile.getLinkInBioLinks()));
        profile.setUser(user);
        return creatorProfileRepository.save(profile);
    }

    public User getMyCreatorProfile() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null)
            throw new RuntimeException("User not found");

        CreatorProfile profile = creatorProfileRepository.findByUser(user);

        if (profile == null)
            throw new RuntimeException("Creator profile not found. Please create one first.");

        return userRepository.findById(user.getId()).orElse(user);
    }

    public CreatorProfile getCreatorProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CreatorProfile profile = creatorProfileRepository.findByUser(user);

        if (profile == null)
            throw new RuntimeException("This user does not have a creator profile.");

        return profile;
    }

    public CreatorPostDTO getCreatorWithPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CreatorProfile profile = creatorProfileRepository.findByUser(user);

        if (profile == null)
            throw new RuntimeException("This user does not have a creator profile.");

        List<Post> posts = postRepository.findByUser_Id(userId);

        return new CreatorPostDTO(profile, posts);
    }

    public List<CreatorProfile> searchByNiche(String niche) {
        List<CreatorProfile> results = creatorProfileRepository.findByNicheIgnoreCase(niche);

        if (results.isEmpty())
            throw new RuntimeException("No creators found for niche: " + niche);

        return results;
    }

    public List<CreatorProfile> getAllCreators() {
        return creatorProfileRepository.findAll();
    }

    public User deleteCreatorProfile() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null)
            throw new RuntimeException("User not found");

        CreatorProfile profile = creatorProfileRepository.findByUser(user);

        if (profile == null)
            throw new RuntimeException("No creator profile found to delete.");

        creatorProfileRepository.delete(profile);
        return userRepository.findById(user.getId()).orElse(user);
    }

    public User updateProfilePicture(String profilePicUrl) {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        CreatorProfile profile = creatorProfileRepository.findByUser(user);
        if (profile == null) {
            throw new RuntimeException("Creator profile not found");
        }

        profile.setProfilepicURL(profilePicUrl);
        creatorProfileRepository.save(profile);
        return userRepository.findById(user.getId()).orElse(user);
    }

    public Map<String, Object> getMyVerifiedEligibility() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Only CREATER users can view creator eligibility");
        }

        CreatorProfile profile = creatorProfileRepository.findByUser(user);
        if (profile == null) {
            throw new RuntimeException("Creator profile not found");
        }

        long followerCount = followingRepository.countByFollowingUser(user);
        long postCount = postRepository.countByUser_Id(user.getId());
        boolean hasBio = profile.getBio() != null && !profile.getBio().isBlank();
        boolean hasProfilePhoto = profile.getProfilepicURL() != null && !profile.getProfilepicURL().isBlank();
        boolean hasCategory = profile.getCreatorCategoryLabel() != null
                && !profile.getCreatorCategoryLabel().isBlank();

        boolean eligible = followerCount >= 100 && postCount >= 5 && hasBio && hasProfilePhoto && hasCategory;

        Map<String, Object> criteria = new LinkedHashMap<>();
        criteria.put("minimumFollowers100", followerCount >= 100);
        criteria.put("minimumPosts5", postCount >= 5);
        criteria.put("hasBio", hasBio);
        criteria.put("hasProfilePhoto", hasProfilePhoto);
        criteria.put("hasCategoryLabel", hasCategory);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("eligible", eligible);
        response.put("followerCount", followerCount);
        response.put("postCount", postCount);
        response.put("criteria", criteria);
        response.put("message", eligible
                ? "Eligible for verified badge review."
                : "Complete missing criteria to become eligible.");
        return response;
    }

    private List<String> sanitizeLinks(List<String> links) {
        if (links == null) {
            return List.of();
        }

        return links.stream()
                .map(link -> link == null ? "" : link.trim())
                .filter(link -> !link.isBlank())
                .limit(8)
                .toList();
    }
}
