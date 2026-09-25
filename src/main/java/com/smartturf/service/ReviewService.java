package com.smartturf.service;

import com.smartturf.dto.ReviewRequest;
import com.smartturf.entity.Review;
import com.smartturf.entity.Turf;
import com.smartturf.entity.User;
import com.smartturf.exception.BookingException;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.ReviewRepository;
import com.smartturf.repository.TurfRepository;
import com.smartturf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service managing customer reviews and star ratings for turfs.
 */
@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TurfRepository turfRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         TurfRepository turfRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.turfRepository = turfRepository;
    }

    // Submit a review
    public Review addReview(ReviewRequest request) {
        // Rule 6: Review rating must be between 1 and 5
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BookingException("Rating must be an integer between 1 and 5 stars.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Turf turf = turfRepository.findById(request.getTurfId())
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + request.getTurfId()));

        Review review = new Review();
        review.setUser(user);
        review.setTurf(turf);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        return reviewRepository.save(review);
    }

    // Get all reviews for a turf
    @Transactional(readOnly = true)
    public List<Review> getReviewsByTurf(Long turfId) {
        return reviewRepository.findByTurfIdOrderByCreatedAtDesc(turfId);
    }

    // Get all reviews for admin
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    // Delete inappropriate review (Admin)
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with ID: " + id);
        }
        reviewRepository.deleteById(id);
    }

    // Get average rating for a turf
    @Transactional(readOnly = true)
    public Double getAverageRating(Long turfId) {
        Double avg = reviewRepository.calculateAverageRating(turfId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }
}
