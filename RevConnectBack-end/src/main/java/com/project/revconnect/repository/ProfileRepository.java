package com.project.revconnect.repository;

import com.project.revconnect.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByfullName(String username);
}
