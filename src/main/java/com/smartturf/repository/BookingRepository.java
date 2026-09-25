package com.smartturf.repository;

import com.smartturf.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Turf Bookings.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings for a specific customer ordered by latest
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find all bookings for a specific turf
    List<Booking> findByTurfIdOrderByCreatedAtDesc(Long turfId);

    // Find bookings by status (CONFIRMED, CANCELLED, PENDING)
    List<Booking> findByStatusOrderByCreatedAtDesc(String status);

    // Find all bookings ordered by creation date descending
    List<Booking> findAllByOrderByCreatedAtDesc();

    // Check if a slot already has an active (non-cancelled) booking
    Optional<Booking> findBySlotIdAndStatus(Long slotId, String status);

    // Count bookings by status for dashboard
    long countByStatus(String status);

    // Calculate total revenue from confirmed bookings
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenue();

    // Count bookings by user
    long countByUserId(Long userId);
}
