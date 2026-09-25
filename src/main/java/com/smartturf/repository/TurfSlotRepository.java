package com.smartturf.repository;

import com.smartturf.entity.TurfSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Spring Data JPA Repository for Turf Slots.
 */
@Repository
public interface TurfSlotRepository extends JpaRepository<TurfSlot, Long> {

    // Find all slots for a turf
    List<TurfSlot> findByTurfId(Long turfId);

    // Find all slots for a turf on a specific date ordered chronologically
    List<TurfSlot> findByTurfIdAndSlotDateOrderByStartTimeAsc(Long turfId, LocalDate slotDate);

    // Find available slots for booking selection
    List<TurfSlot> findByTurfIdAndSlotDateAndStatusOrderByStartTimeAsc(Long turfId, LocalDate slotDate, String status);

    // Find slots by date
    List<TurfSlot> findBySlotDate(LocalDate slotDate);

    // Prevent duplicate slot times for same turf and date
    boolean existsByTurfIdAndSlotDateAndStartTime(Long turfId, LocalDate slotDate, LocalTime startTime);

    // Delete all slots belonging to a turf
    void deleteByTurfId(Long turfId);
}
