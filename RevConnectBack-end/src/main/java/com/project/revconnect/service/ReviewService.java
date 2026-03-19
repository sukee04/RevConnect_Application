package com.project.revconnect.service;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.Review;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.ReviewRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    public Review addReview(Long businessUserId, int rating, String comment) {

        User reviewer = getLoggedUser();

        if (reviewer.getRole() == Handlers.Business_Account_User) {
            throw new RuntimeException("Business users cannot give reviews");
        }

        User businessUser = userRepository.findById(businessUserId)
                .orElseThrow(() -> new RuntimeException("Business user not found"));

        if (businessUser.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Target user is not business account");
        }

        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setBusinessUser(businessUser);
        review.setReviewer(reviewer);

        return reviewRepository.save(review);
    }

    public List<Review> getBusinessReviews() {
        User businessUser = getLoggedUser();

        if (businessUser.getRole() != Handlers.Business_Account_User) {
            throw new RuntimeException("Only business users can view reviews");
        }

        return reviewRepository.findByBusinessUser(businessUser);
    }

    public List<Review> getReviewsByBusinessUserId(Long businessUserId) {
        User targetUser = userRepository.findById(businessUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reviewRepository.findByBusinessUser(targetUser);
    }

    public String deleteReview(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        reviewRepository.delete(review);
        return "Review deleted successfully";
    }
}