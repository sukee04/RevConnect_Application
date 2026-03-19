package com.project.revconnect.repository;

import com.project.revconnect.model.CreatorProfile;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {
    CreatorProfile findByUser(User user);
    List<CreatorProfile> findByNicheIgnoreCase(String niche);
}