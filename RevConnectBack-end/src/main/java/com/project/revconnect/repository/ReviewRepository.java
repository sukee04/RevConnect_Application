package com.project.revconnect.repository;

import com.project.revconnect.model.Review;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBusinessUser(User user);

    long countByBusinessUser(User user);
}