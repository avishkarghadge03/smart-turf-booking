package com.smartturf.controller;

import com.smartturf.dto.ApiResponse;
import com.smartturf.dto.ReviewRequest;
import com.smartturf.entity.Review;
import com.smartturf.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Customer Reviews and Turf Ratings.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Submit a review: POST /api/reviews
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(@Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.addReview(request);
        return new ResponseEntity<>(
                ApiResponse.ok("Review submitted successfully!", review),
                HttpStatus.CREATED
        );
    }

    // Get all reviews (Admin): GET /api/reviews
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // Get reviews for a turf: GET /api/reviews/turf/{turfId}
    @GetMapping("/turf/{turfId}")
    public ResponseEntity<List<Review>> getReviewsByTurf(@PathVariable Long turfId) {
        return ResponseEntity.ok(reviewService.getReviewsByTurf(turfId));
    }

    // Get average rating for a turf: GET /api/reviews/turf/{turfId}/average
    @GetMapping("/turf/{turfId}/average")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long turfId) {
        Double avg = reviewService.getAverageRating(turfId);
        return ResponseEntity.ok(Map.of("turfId", turfId, "averageRating", avg));
    }

    // Delete inappropriate review (Admin): DELETE /api/reviews/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully."));
    }
}
