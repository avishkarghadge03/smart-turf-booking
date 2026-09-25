package com.smartturf.repository;

import com.smartturf.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Payments.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment record by booking ID
    Optional<Payment> findByBookingId(Long bookingId);

    // Find payment by unique transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find payments by status (SUCCESS, FAILED, PENDING)
    List<Payment> findByPaymentStatusOrderByPaymentDateDesc(String paymentStatus);

    // List all payments ordered by latest date
    List<Payment> findAllByOrderByPaymentDateDesc();

    // Calculate total successful payments for revenue metrics
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    BigDecimal calculateTotalSuccessfulRevenue();
}
