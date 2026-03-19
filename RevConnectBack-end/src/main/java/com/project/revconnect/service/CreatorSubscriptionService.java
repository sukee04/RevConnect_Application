package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.CreatorSubscription;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.CreatorSubscriptionRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreatorSubscriptionService {

    private final CreatorSubscriptionRepository creatorSubscriptionRepository;
    private final UserRepository userRepository;

    public CreatorSubscriptionService(CreatorSubscriptionRepository creatorSubscriptionRepository,
            UserRepository userRepository) {
        this.creatorSubscriptionRepository = creatorSubscriptionRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    public Map<String, Object> subscribeToCreator(Long creatorId) {
        User subscriber = getLoggedInUser();
        if (subscriber == null) {
            throw new RuntimeException("Not authenticated");
        }
        if (subscriber.getId().equals(creatorId)) {
            throw new RuntimeException("You cannot subscribe to yourself");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        if (creator.getRole() != Handlers.CREATER) {
            throw new RuntimeException("Subscriptions are available for creator profiles only");
        }

        CreatorSubscription subscription = creatorSubscriptionRepository
                .findByCreator_IdAndSubscriber_Id(creatorId, subscriber.getId())
                .orElseGet(CreatorSubscription::new);

        subscription.setCreator(creator);
        subscription.setSubscriber(subscriber);
        subscription.setStatus("ACTIVE");
        creatorSubscriptionRepository.save(subscription);

        return Map.of("message", "Subscribed successfully");
    }

    public Map<String, Object> unsubscribeFromCreator(Long creatorId) {
        User subscriber = getLoggedInUser();
        if (subscriber == null) {
            throw new RuntimeException("Not authenticated");
        }

        CreatorSubscription subscription = creatorSubscriptionRepository
                .findByCreator_IdAndSubscriber_Id(creatorId, subscriber.getId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        creatorSubscriptionRepository.delete(subscription);
        return Map.of("message", "Unsubscribed successfully");
    }

    public Map<String, Object> getSubscriptionStatus(Long creatorId) {
        User subscriber = getLoggedInUser();
        if (subscriber == null) {
            throw new RuntimeException("Not authenticated");
        }

        boolean subscribed = creatorSubscriptionRepository
                .existsByCreator_IdAndSubscriber_IdAndStatus(creatorId, subscriber.getId(), "ACTIVE");

        return Map.of("subscribed", subscribed);
    }

    public List<Map<String, Object>> getMySubscriptions() {
        User user = getLoggedInUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }

        return creatorSubscriptionRepository.findBySubscriber_IdAndStatus(user.getId(), "ACTIVE")
                .stream()
                .map(subscription -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("creatorId", subscription.getCreator().getId());
                    data.put("creatorUsername", subscription.getCreator().getUsername());
                    data.put("createdAt", subscription.getCreatedAt());
                    return data;
                })
                .toList();
    }
}

