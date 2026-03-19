package com.project.revconnect.service;

import com.project.revconnect.model.User;
import com.project.revconnect.model.UserProfile;
import com.project.revconnect.repository.ProfileRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public UserProfile addProfile(Long userId, UserProfile profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (user.getUserProfile() != null) {
            UserProfile existing = user.getUserProfile();
            if (profile.getFullName() != null)
                existing.setFullName(profile.getFullName());
            if (profile.getBio() != null)
                existing.setBio(profile.getBio());
            if (profile.getLocation() != null)
                existing.setLocation(profile.getLocation());
            if (profile.getAge() != null)
                existing.setAge(profile.getAge());
            if (profile.getGender() != null)
                existing.setGender(profile.getGender());
            if (profile.getProfilepicURL() != null)
                existing.setProfilepicURL(profile.getProfilepicURL());
            return profileRepository.save(existing);
        }


        if (profile.getFullName() == null || profile.getFullName().isBlank()) {
            profile.setFullName(user.getUsername());
        }
        profile.setUser(user);
        return profileRepository.save(profile);
    }

    public User getUserByName(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            user = userRepository.findByEmail(username);
        if (user == null)
            throw new RuntimeException("User not found: " + username);
        return user;
    }

    public User updateProfilePicture(String username, String profilePicUrl) {
        User user = getUserByName(username);
        UserProfile profile = user.getUserProfile();


        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            profile.setFullName(user.getUsername());
        }

        profile.setProfilepicURL(profilePicUrl);
        profileRepository.save(profile);
        return userRepository.findById(user.getId()).orElse(user);
    }

    public UserProfile updatePrivacy(String username, boolean isPublic) {
        User user = getUserByName(username);
        UserProfile profile = user.getUserProfile();

        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            profile.setFullName(user.getUsername());
        }

        profile.setPublic(isPublic);
        return profileRepository.save(profile);
    }
}
