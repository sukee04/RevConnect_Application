package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.BusinessProfile;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.BusinessProfileRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository repository;
    private final UserRepository userRepository;

    public BusinessProfileService(BusinessProfileRepository repository,
            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    private User getLoggedBusinessUser() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null) {
            throw new RuntimeException("Only Business Users allowed");
        }
        if (user.getRole() != null && user.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only Business Users allowed");
        }

        return user;
    }

    public BusinessProfile createOrUpdate(BusinessProfile profile) {

        User user = getLoggedBusinessUser();

        BusinessProfile existing = repository.findByUser(user).orElse(null);

        if (existing != null) {
            existing.setBusinessName(trimToLength(profile.getBusinessName(), 160));
            existing.setBusinessCategory(trimToLength(profile.getBusinessCategory(), 120));
            existing.setDescription(trimToLength(profile.getDescription(), 2000));
            existing.setWebsite(trimToLength(profile.getWebsite(), 300));
            existing.setContactEmail(trimToLength(profile.getContactEmail(), 180));
            existing.setContactPhone(trimToLength(profile.getContactPhone(), 40));
            existing.setLogoUrl(profile.getLogoUrl());
            existing.setBusinessAddress(trimToLength(profile.getBusinessAddress(), 300));
            existing.setBusinessHours(trimToLength(profile.getBusinessHours(), 200));
            existing.setExternalLinks(trimToLength(profile.getExternalLinks(), 2000));
            existing.setAllowMessages(profile.isAllowMessages());
            existing.setPublic(profile.isPublic());
            return repository.save(existing);
        }

        profile.setBusinessName(trimToLength(profile.getBusinessName(), 160));
        profile.setBusinessCategory(trimToLength(profile.getBusinessCategory(), 120));
        profile.setDescription(trimToLength(profile.getDescription(), 2000));
        profile.setWebsite(trimToLength(profile.getWebsite(), 300));
        profile.setContactEmail(trimToLength(profile.getContactEmail(), 180));
        profile.setContactPhone(trimToLength(profile.getContactPhone(), 40));
        profile.setBusinessAddress(trimToLength(profile.getBusinessAddress(), 300));
        profile.setBusinessHours(trimToLength(profile.getBusinessHours(), 200));
        profile.setExternalLinks(trimToLength(profile.getExternalLinks(), 2000));
        profile.setUser(user);
        profile.setVerified(false);
        return repository.save(profile);
    }

    public User getMyProfile() {
        User user = getLoggedBusinessUser();
        repository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return userRepository.findById(user.getId()).orElse(user);
    }

    public User deleteProfile() {
        User user = getLoggedBusinessUser();
        BusinessProfile profile = repository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        repository.delete(profile);
        return userRepository.findById(user.getId()).orElse(user);
    }

    public User updateProfilePicture(String logoUrl) {
        User user = getLoggedBusinessUser();
        BusinessProfile profile = repository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        profile.setLogoUrl(logoUrl);
        repository.save(profile);
        return userRepository.findById(user.getId()).orElse(user);
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }
}
