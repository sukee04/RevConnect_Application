package com.project.revconnect.service;

import com.project.revconnect.model.Post;
import com.project.revconnect.model.SavedPost;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.PostRepository;
import com.project.revconnect.repository.SavedPostRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavedPostService {

    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public SavedPostService(SavedPostRepository savedPostRepository, PostRepository postRepository,
            UserRepository userRepository) {
        this.savedPostRepository = savedPostRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    @Transactional
    public String savePost(Long postId) {
        User user = getLoggedInUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }
        if (savedPostRepository.existsByUser_IdAndPost_Id(user.getId(), postId)) {
            return "Post is already saved";
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        SavedPost savedPost = new SavedPost(user, post);
        savedPostRepository.save(savedPost);
        return "Post saved successfully";
    }

    @Transactional
    public String unsavePost(Long postId) {
        User user = getLoggedInUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }
        savedPostRepository.deleteByUser_IdAndPost_Id(user.getId(), postId);
        return "Post removed from saved collection";
    }

    public List<SavedPost> getMySavedPosts() {
        User user = getLoggedInUser();
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }
        return savedPostRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    public long getSaveCount(Long postId) {
        return savedPostRepository.countByPost_Id(postId);
    }

    public boolean isSavedByUser(Long postId) {
        User user = getLoggedInUser();
        if (user == null) {
            return false;
        }
        return savedPostRepository.existsByUser_IdAndPost_Id(user.getId(), postId);
    }
}
