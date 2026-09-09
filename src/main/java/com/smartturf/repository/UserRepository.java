package com.smartturf.repository;

import com.smartturf.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by email for authentication and checks
    Optional<User> findByEmail(String email);

    // Check if an email is already registered
    boolean existsByEmail(String email);

    // Find users by role (e.g. CUSTOMER or ADMIN)
    List<User> findByRoleOrderByCreatedAtDesc(String role);

    // Count users by role for dashboard stats
    long countByRole(String role);
}
