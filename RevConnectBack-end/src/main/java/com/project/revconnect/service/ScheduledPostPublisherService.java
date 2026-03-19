package com.project.revconnect.service;

import com.project.revconnect.model.Post;
import com.project.revconnect.repository.PostRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledPostPublisherService {

    private final PostRepository postRepository;

    public ScheduledPostPublisherService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void publishDuePosts() {
        List<Post> duePosts = postRepository.findByPublishedFalseAndScheduledAtLessThanEqual(LocalDateTime.now());
        if (duePosts.isEmpty()) {
            return;
        }

        duePosts.forEach(post -> post.setPublished(true));
        postRepository.saveAll(duePosts);
    }
}

