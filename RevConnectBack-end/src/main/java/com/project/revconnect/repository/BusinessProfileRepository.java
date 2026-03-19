package com.project.revconnect.repository;

import com.project.revconnect.model.BusinessProfile;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByUser(User user);
}