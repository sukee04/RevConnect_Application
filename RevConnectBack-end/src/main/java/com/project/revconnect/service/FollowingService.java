package com.project.revconnect.service;

import com.project.revconnect.dto.FollowerResponseDTO;
import com.project.revconnect.dto.FollowingResponseDTO;
import com.project.revconnect.model.Following;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.FollowingRepository;
import com.project.revconnect.repository.UserRepository;
import com.project.revconnect.util.AuthUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowingService {

        private final FollowingRepository followingRepository;
        private final UserRepository userRepository;
        private final NotificationService notificationService;

        public FollowingService(FollowingRepository followingRepository,
                                UserRepository userRepository,
                                NotificationService notificationService) {
                this.followingRepository = followingRepository;
                this.userRepository = userRepository;
                this.notificationService = notificationService;
        }

        public String followUser(Long followId) {
                User user = AuthUtil.getLoggedInUser(userRepository);
                if (user == null) return "Not authenticated";

                User followUser = userRepository.findById(followId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getId().equals(followId)) return "You cannot follow yourself";


                if (followingRepository.findByUserAndFollowingUser(user, followUser) != null) {
                        return "Already Following";
                }

                Following following = new Following();
                following.setUser(user);
                following.setFollowingUser(followUser);
                followingRepository.save(following);
                notificationService.createNotification(followUser, user, "FOLLOW", following.getId());
                return "Followed Successfully";
        }

        public String unfollowUser(Long followId) {
                User user = AuthUtil.getLoggedInUser(userRepository);
                if (user == null) return "Not authenticated";

                User followUser = userRepository.findById(followId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Following existing = followingRepository.findByUserAndFollowingUser(user, followUser);
                if (existing == null) return "Not Following";

                followingRepository.delete(existing);
                return "Unfollowed Successfully";
        }

        public String unfollowUserByUsername(String username) {
                User followUser = userRepository.findByUsername(username);
                if (followUser == null) {
                        throw new RuntimeException("User not found");
                }
                return unfollowUser(followUser.getId());
        }

        public List<FollowingResponseDTO> getFollowingList() {
                User user = AuthUtil.getLoggedInUser(userRepository);
                return getFollowingList(user);
        }

        public List<FollowingResponseDTO> getFollowingList(User targetUser) {
                if (targetUser == null) {
                        return List.of();
                }
                return followingRepository.findByUser(targetUser).stream()
                        .map(f -> new FollowingResponseDTO(f.getFollowingUser().getId(), f.getFollowingUser().getUsername()))
                        .toList();
        }

        public List<FollowingResponseDTO> getFollowingListByUsername(String username) {
                User targetUser = userRepository.findByUsername(username);
                if (targetUser == null) {
                        throw new RuntimeException("User not found");
                }
                return getFollowingList(targetUser);
        }

        public List<FollowerResponseDTO> getFollowersList() {
                User user = AuthUtil.getLoggedInUser(userRepository);
                return getFollowersList(user);
        }

        public List<FollowerResponseDTO> getFollowersList(User targetUser) {
                if (targetUser == null) {
                        return List.of();
                }
                return followingRepository.findByFollowingUser(targetUser).stream()
                        .map(f -> new FollowerResponseDTO(f.getUser().getId(), f.getUser().getUsername()))
                        .toList();
        }

        public List<FollowerResponseDTO> getFollowersListByUsername(String username) {
                User targetUser = userRepository.findByUsername(username);
                if (targetUser == null) {
                        throw new RuntimeException("User not found");
                }
                return getFollowersList(targetUser);
        }

        public long getFollowersCount() {
                User user = AuthUtil.getLoggedInUser(userRepository);
                return getFollowersCount(user);
        }

        public long getFollowingCount() {
                User user = AuthUtil.getLoggedInUser(userRepository);
                return getFollowingCount(user);
        }

        public long getFollowersCount(User targetUser) {
                if (targetUser == null) {
                        return 0;
                }
                return followingRepository.countByFollowingUser(targetUser);
        }

        public long getFollowingCount(User targetUser) {
                if (targetUser == null) {
                        return 0;
                }
                return followingRepository.countByUser(targetUser);
        }

        public long getFollowersCountByUsername(String username) {
                User targetUser = userRepository.findByUsername(username);
                if (targetUser == null) {
                        throw new RuntimeException("User not found");
                }
                return getFollowersCount(targetUser);
        }

        public long getFollowingCountByUsername(String username) {
                User targetUser = userRepository.findByUsername(username);
                if (targetUser == null) {
                        throw new RuntimeException("User not found");
                }
                return getFollowingCount(targetUser);
        }
}
