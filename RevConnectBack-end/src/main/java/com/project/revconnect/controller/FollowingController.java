package com.project.revconnect.controller;

import com.project.revconnect.dto.FollowerResponseDTO;
import com.project.revconnect.dto.FollowingResponseDTO;
import com.project.revconnect.service.FollowingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/revconnect/users", "/following"})
public class FollowingController {

    private FollowingService followingService;

    public FollowingController(FollowingService followingService) {
        this.followingService = followingService;
    }

    @PostMapping({"/following/{followId}", "/follow/{followId}"})
    public String followUser(@PathVariable Long followId) {
        return followingService.followUser(followId);
    }

    @DeleteMapping({"/following/{followId}", "/unfollow/{followId}"})
    public String unfollowUser(@PathVariable Long followId) {
        return followingService.unfollowUser(followId);
    }

    @DeleteMapping("/following/username/{username}")
    public String unfollowUserByUsername(@PathVariable String username) {
        return followingService.unfollowUserByUsername(username);
    }

    @GetMapping({"/following", "/list"})
    public List<FollowingResponseDTO> getFollowing() {
        return followingService.getFollowingList();
    }

    @GetMapping("/following/{username}")
    public List<FollowingResponseDTO> getFollowingByUsername(@PathVariable String username) {
        return followingService.getFollowingListByUsername(username);
    }

    @GetMapping("/followers")
    public List<FollowerResponseDTO> getFollowers() {
        return followingService.getFollowersList();
    }

    @GetMapping("/followers/{username}")
    public List<FollowerResponseDTO> getFollowersByUsername(@PathVariable String username) {
        return followingService.getFollowersListByUsername(username);
    }

    @GetMapping("/followers/count")
    public long getFollowersCount() {
        return followingService.getFollowersCount();
    }

    @GetMapping("/followers/count/{username}")
    public long getFollowersCountByUsername(@PathVariable String username) {
        return followingService.getFollowersCountByUsername(username);
    }

    @GetMapping("/following/count")
    public long getFollowingCount() {
        return followingService.getFollowingCount();
    }

    @GetMapping("/following/count/{username}")
    public long getFollowingCountByUsername(@PathVariable String username) {
        return followingService.getFollowingCountByUsername(username);
    }
}
