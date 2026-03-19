package com.project.revconnect.repository;

import com.project.revconnect.model.StoryViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryViewEventRepository extends JpaRepository<StoryViewEvent, Long> {
    long countByStory_User_Id(Long creatorId);
    long countByStory_User_IdAndTapThroughTrue(Long creatorId);
    List<StoryViewEvent> findByStory_IdOrderByViewedAtDesc(Long storyId);
    void deleteByViewer_Id(Long viewerId);
    void deleteByStory_User_Id(Long userId);
}
