package com.smartturf.repository;

import com.smartturf.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA Repository for Turf Customer Reviews.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a specific turf ordered by newest
    List<Review> findByTurfIdOrderByCreatedAtDesc(Long turfId);

    // Find all reviews written by a customer
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find all reviews for admin moderation
    List<Review> findAllByOrderByCreatedAtDesc();

    // Calculate average rating for a turf
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.turf.id = :turfId")
    Double calculateAverageRating(@Param("turfId") Long turfId);

    // Count reviews for a turf
    long countByTurfId(Long turfId);
}
