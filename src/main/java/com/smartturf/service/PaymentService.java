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

        // Rule 5: Payment amount must match booking amount
        if (booking.getTotalAmount().compareTo(request.getAmount()) != 0) {
            throw new BookingException("Payment amount (" + request.getAmount() +
                    ") does not match booking total amount (" + booking.getTotalAmount() + ").");
        }

        // Check if payment already exists for this booking
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BookingException("Payment has already been processed for Booking #" + booking.getId());
        }

        // Generate simulated transaction ID (e.g. TXN9810237461)
        String transactionId = "TXN" + System.currentTimeMillis() + (int)(Math.random() * 900 + 100);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        payment.setTransactionId(transactionId);
        payment.setPaymentStatus("SUCCESS");

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status to CONFIRMED
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // Send payment confirmation notification
        notificationService.createNotification(
                booking.getUser(),
                "Payment of ₹" + payment.getAmount() + " via " + payment.getPaymentMethod() +
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
