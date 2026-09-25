package com.smartturf.service;

import com.smartturf.dto.PaymentRequest;
import com.smartturf.entity.Booking;
import com.smartturf.entity.Payment;
import com.smartturf.exception.BookingException;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.BookingRepository;
import com.smartturf.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Service managing simulated payment processing for bookings.
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    // Process a simulated payment
    public Payment processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new BookingException("Cannot process payment for a cancelled booking.");
        }

        // Check if already paid/confirmed
        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new BookingException("Payment has already been confirmed for Booking #" + booking.getId());
        }

        // Always use the actual booking amount from DB as the authoritative amount
        // This prevents mismatch errors when frontend sends slightly different float values
        java.math.BigDecimal authorizedAmount = booking.getTotalAmount();

        // Reuse existing payment record if previously failed, or create new
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElse(new Payment());

        // Handle simulated failure
        if (Boolean.TRUE.equals(request.getSimulateFailure())) {
            String failTxnId = "TXNFAIL" + System.currentTimeMillis() + (int)(Math.random() * 900 + 100);
            payment.setBooking(booking);
            payment.setAmount(authorizedAmount);
            payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "UPI");
            payment.setTransactionId(failTxnId);
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);

            notificationService.createNotification(
                    booking.getUser(),
                    "Payment simulation failed for Booking #" + booking.getId() + ". You can retry payment from My Bookings.",
                    "PAYMENT"
            );
            throw new BookingException("Simulated payment failed (Transaction declined). Booking #" +
                    booking.getId() + " is held as PENDING. You can retry payment.");
        }

        // Generate simulated transaction ID
        String transactionId = "TXN" + System.currentTimeMillis() + (int)(Math.random() * 900 + 100);

        payment.setBooking(booking);
        payment.setAmount(authorizedAmount);
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "UPI");
        payment.setTransactionId(transactionId);
        payment.setPaymentStatus("SUCCESS");

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status to CONFIRMED
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // Send payment confirmation notification
        notificationService.createNotification(
                booking.getUser(),
                "Payment of ₹" + authorizedAmount + " via " + payment.getPaymentMethod() +
                        " was successful! Booking #" + booking.getId() + " is CONFIRMED. Transaction ID: " + transactionId,
                "PAYMENT"
        );

        return savedPayment;
    }

    // Get all payments (for Admin)
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByPaymentDateDesc();
    }

    // Get payment by ID
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + id));
    }

    // Get payment by Booking ID
    @Transactional(readOnly = true)
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for Booking ID: " + bookingId));
    }
}
