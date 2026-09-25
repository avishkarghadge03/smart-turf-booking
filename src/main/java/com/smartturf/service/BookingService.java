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

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          TurfRepository turfRepository,
                          TurfSlotRepository turfSlotRepository,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.turfRepository = turfRepository;
        this.turfSlotRepository = turfSlotRepository;
        this.notificationService = notificationService;
    }

    // Create a new booking with strict duplicate booking prevention
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

        // 4. DUPLICATE BOOKING PREVENTION (Rules 2 & 3)
        // Check slot status
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
