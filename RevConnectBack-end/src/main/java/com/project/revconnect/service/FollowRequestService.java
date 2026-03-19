package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.FollowRequest;
import com.project.revconnect.model.Following;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.FollowRequestRepository;
import com.project.revconnect.repository.FollowingRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowRequestService {

    private final FollowRequestRepository followRequestRepository;
    private final UserRepository userRepository;
    private final FollowingService followingService;
    private final NotificationService notificationService;
    private final FollowingRepository followingRepository;

    public FollowRequestService(FollowRequestRepository followRequestRepository,
                                UserRepository userRepository, FollowingService followingService,
                                NotificationService notificationService, FollowingRepository followingRepository) {
        this.followRequestRepository = followRequestRepository;
        this.userRepository = userRepository;
        this.followingService = followingService;
        this.notificationService = notificationService;
        this.followingRepository = followingRepository;
    }

    private User getLoggedInUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    @Transactional
    public String sendFollowRequest(Long receiverId) {
        User sender = getLoggedInUser();
        if (sender == null) throw new RuntimeException("Not authenticated");
        if (sender.getId().equals(receiverId)) return "Cannot follow yourself";

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (followingRepository.findByUserAndFollowingUser(sender, receiver) != null) {
            return "Already Following";
        }


        boolean isBusinessOrCreator = receiver.getRole() == Handlers.Business_Account_User
                || receiver.getRole() == Handlers.CREATER;


        boolean isPublicPersonal = receiver.getUserProfile() != null && receiver.getUserProfile().isPublic();

        if (isBusinessOrCreator || isPublicPersonal) {
            followingService.followUser(receiverId);
            return "Followed successfully";
        }


        boolean exists = followRequestRepository.existsBySender_IdAndReceiver_IdAndStatus(
                sender.getId(), receiverId, "PENDING");
        if (exists) return "Follow request already sent";

        FollowRequest request = new FollowRequest(sender, receiver);
        followRequestRepository.save(request);
        notificationService.createNotification(receiver, sender, "FOLLOW_REQUEST", request.getId());
        return "Follow request sent";
    }

    public List<FollowRequest> getPendingRequests() {
        User user = getLoggedInUser();

        return followRequestRepository.findByReceiver_IdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING");
    }

    public List<FollowRequest> getSentPendingRequests() {
        User user = getLoggedInUser();
        return followRequestRepository.findBySender_IdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING");
    }

    @Transactional
    public String acceptRequest(Long requestId) {
        User user = getLoggedInUser();
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getReceiver().getId().equals(user.getId())) throw new RuntimeException("Unauthorized");

        request.setStatus("ACCEPTED");
        followRequestRepository.save(request);


        User sender = request.getSender();
        if (followingRepository.findByUserAndFollowingUser(sender, user) == null) {
            Following following = new Following();
            following.setUser(sender);
            following.setFollowingUser(user);
            followingRepository.save(following);
        }

        notificationService.createNotification(sender, user, "FOLLOW_ACCEPTED", request.getId());
        return "Follow request accepted";
    }

    @Transactional
    public String rejectRequest(Long requestId) {
        User user = getLoggedInUser();
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getReceiver().getId().equals(user.getId())) throw new RuntimeException("Unauthorized");

        request.setStatus("REJECTED");
        followRequestRepository.save(request);
        return "Follow request rejected";
    }
}
