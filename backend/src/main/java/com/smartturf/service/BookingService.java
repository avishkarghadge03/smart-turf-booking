package com.smartturf.service;

import com.smartturf.dto.BookingRequest;
import com.smartturf.entity.Booking;
import com.smartturf.entity.Turf;
import com.smartturf.entity.TurfSlot;
import com.smartturf.entity.User;
import com.smartturf.exception.BookingException;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.BookingRepository;
import com.smartturf.repository.TurfRepository;
import com.smartturf.repository.TurfSlotRepository;
import com.smartturf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service managing customer turf reservations, slot locking, and cancellations.
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TurfRepository turfRepository;
    private final TurfSlotRepository turfSlotRepository;
    private final NotificationService notificationService;
    private final com.smartturf.repository.PaymentRepository paymentRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          TurfRepository turfRepository,
                          TurfSlotRepository turfSlotRepository,
                          NotificationService notificationService,
                          com.smartturf.repository.PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.turfRepository = turfRepository;
        this.turfSlotRepository = turfSlotRepository;
        this.notificationService = notificationService;
        this.paymentRepository = paymentRepository;
    }

    // Create a new booking with strict duplicate booking prevention (Legacy flow, retained for tests)
    public Booking createBooking(BookingRequest request) {
        // 1. Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // 2. Verify turf exists
        Turf turf = turfRepository.findById(request.getTurfId())
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + request.getTurfId()));

        if (!"AVAILABLE".equalsIgnoreCase(turf.getStatus())) {
            throw new BookingException("This turf is currently unavailable for bookings.");
        }

        // 3. Verify slot exists
        TurfSlot slot = turfSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + request.getSlotId()));

        // Verify slot belongs to the requested turf
        if (!slot.getTurf().getId().equals(turf.getId())) {
            throw new BookingException("Selected slot does not belong to the chosen turf.");
        }

        // 4. DUPLICATE BOOKING PREVENTION
        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            throw new BookingException("Turf slot is already booked.");
        }

        // Change slot status: AVAILABLE -> BOOKED
        slot.setStatus("BOOKED");
        turfSlotRepository.save(slot);

        // Determine total amount
        BigDecimal totalAmount = request.getTotalAmount() != null && request.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.getTotalAmount()
                : turf.getPricePerHour();

        // Create booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTurf(turf);
        booking.setSlot(slot);
        booking.setBookingDate(request.getBookingDate() != null ? request.getBookingDate() : slot.getSlotDate());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("PENDING");

        Booking savedBooking = bookingRepository.save(booking);

        // Send notification to customer
        notificationService.createNotification(
                user,
                "Booking #" + savedBooking.getId() + " placed for " + turf.getName() + " on " + slot.getSlotDate() +
                        " (" + slot.getStartTime() + " - " + slot.getEndTime() + "). Complete payment to confirm.",
                "BOOKING"
        );

        return savedBooking;
    }

    // One-click instant checkout: Create booking, update slot, process payment all in one transaction
    public java.util.Map<String, Object> instantCheckout(BookingRequest request) {
        // 1. Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // 2. Verify turf exists
        Turf turf = turfRepository.findById(request.getTurfId())
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + request.getTurfId()));

        if (!"AVAILABLE".equalsIgnoreCase(turf.getStatus())) {
            throw new BookingException("This turf is currently unavailable for bookings.");
        }

        // 3. Verify slot exists
        TurfSlot slot = turfSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + request.getSlotId()));

        // Verify slot belongs to the requested turf
        if (!slot.getTurf().getId().equals(turf.getId())) {
            throw new BookingException("Selected slot does not belong to the chosen turf.");
        }

        // 4. DUPLICATE BOOKING PREVENTION
        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            throw new BookingException("Sorry, this slot is no longer available. Please select another slot.");
        }

        // Mark slot as booked
        slot.setStatus("BOOKED");
        turfSlotRepository.save(slot);

        // Determine total amount
        BigDecimal totalAmount = turf.getPricePerHour(); // Always trust backend price

        // 5. Create booking entity and confirm immediately
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTurf(turf);
        booking.setSlot(slot);
        booking.setBookingDate(request.getBookingDate() != null ? request.getBookingDate() : slot.getSlotDate());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED"); // CONFIRM IMMEDIATELY
        Booking savedBooking = bookingRepository.save(booking);

        // 6. Create simulated UPI payment
        String transactionId = "UPI-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        com.smartturf.entity.Payment payment = new com.smartturf.entity.Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod("UPI");
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionId(transactionId);
        paymentRepository.save(payment);

        // Send notification to customer
        notificationService.createNotification(
                user,
                "Payment of ₹" + totalAmount + " via UPI was successful! Booking #" + savedBooking.getId() + " placed for " + turf.getName() + " on " + slot.getSlotDate() +
                        " (" + slot.getStartTime() + " - " + slot.getEndTime() + "). Transaction ID: " + transactionId,
                "BOOKING"
        );

        // 7. Prepare Response Map
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("bookingId", savedBooking.getId());
        response.put("bookingStatus", savedBooking.getStatus());
        response.put("paymentStatus", payment.getPaymentStatus());
        response.put("paymentMethod", payment.getPaymentMethod());
        response.put("amount", totalAmount);
        response.put("transactionId", transactionId);
        response.put("bookingDate", savedBooking.getBookingDate() != null ? savedBooking.getBookingDate().toString() : slot.getSlotDate().toString());

        return response;
    }

    // Cancel a booking and release the slot (Rule 4)
    public Booking cancelBooking(Long id) {
        Booking booking = getBookingById(id);

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new BookingException("Booking is already cancelled.");
        }

        // Update booking status
        booking.setStatus("CANCELLED");

        // Release slot: BOOKED -> AVAILABLE
        TurfSlot slot = booking.getSlot();
        if (slot != null) {
            slot.setStatus("AVAILABLE");
            turfSlotRepository.save(slot);
        }

        Booking updatedBooking = bookingRepository.save(booking);

        // Send cancellation notification
        notificationService.createNotification(
                booking.getUser(),
                "Your booking #" + booking.getId() + " for " + booking.getTurf().getName() + " has been cancelled.",
                "BOOKING"
        );

        return updatedBooking;
    }

    // Get all bookings (Admin view)
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get booking by ID
    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }

    // Get bookings for a customer
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Update booking status
    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = getBookingById(id);
        String oldStatus = booking.getStatus();
        booking.setStatus(status.toUpperCase());

        // If status changed to CANCELLED, release slot
        if ("CANCELLED".equalsIgnoreCase(status) && !"CANCELLED".equalsIgnoreCase(oldStatus)) {
            if (booking.getSlot() != null) {
                booking.getSlot().setStatus("AVAILABLE");
                turfSlotRepository.save(booking.getSlot());
            }
        }

        return bookingRepository.save(booking);
    }

    // Delete booking
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        // Release slot if not already cancelled
        if (!"CANCELLED".equalsIgnoreCase(booking.getStatus()) && booking.getSlot() != null) {
            booking.getSlot().setStatus("AVAILABLE");
            turfSlotRepository.save(booking.getSlot());
        }
        bookingRepository.delete(booking);
    }
}
