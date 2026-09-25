package com.smartturf.repository;

import com.smartturf.entity.Turf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA Repository for Turf arena operations.
 */
@Repository
public interface TurfRepository extends JpaRepository<Turf, Long> {

    // Find all turfs by availability status
    List<Turf> findByStatus(String status);

    // Search turfs by sport type
    List<Turf> findBySportTypeContainingIgnoreCase(String sportType);

    // Filter by location
    List<Turf> findByLocationContainingIgnoreCase(String location);

    // Comprehensive search across name, location, and sport type
    @Query("SELECT t FROM Turf t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.sportType) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Turf> searchTurfs(@Param("query") String query);

    // Count turfs by status for admin dashboard
    long countByStatus(String status);
}
