package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.Review;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.ProductRepository;
import com.project.revconnect.repository.ReviewRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public AnalyticsService(ProductRepository productRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    private User getBusinessUser() {
        User user = com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);

        if (user == null || user.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business users allowed");
        }

        return user;
    }

    public Map<String, Object> getDashboard() {

        User businessUser = getBusinessUser();

        long totalProducts = productRepository.findByUser(businessUser).size();
        long totalReviews = reviewRepository.countByBusinessUser(businessUser);

        List<Review> reviews = reviewRepository.findByBusinessUser(businessUser);

        double avgRating = 0;

        if (!reviews.isEmpty()) {
            avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0);
        }

        return Map.of(
                "totalProducts", totalProducts,
                "totalReviews", totalReviews,
                "averageRating", avgRating);
    }
}