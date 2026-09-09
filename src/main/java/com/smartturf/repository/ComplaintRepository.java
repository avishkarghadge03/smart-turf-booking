package com.smartturf.repository;

import com.smartturf.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA Repository for Customer Complaints.
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find all complaints submitted by a user
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find complaints by status (OPEN, IN_PROGRESS, RESOLVED)
    List<Complaint> findByStatusOrderByCreatedAtDesc(String status);

    // List all complaints for admin
    List<Complaint> findAllByOrderByCreatedAtDesc();

    // Count complaints by status (for dashboard open complaints card)
    long countByStatus(String status);
}
