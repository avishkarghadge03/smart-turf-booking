package com.smartturf;

import com.smartturf.dto.*;
import com.smartturf.entity.*;
import com.smartturf.exception.BookingException;
import com.smartturf.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Backend Unit & Integration Tests.
 * Tests all core business logic and rules for BSc IT project evaluation:
 * - User Registration & Unique Email Rule
 * - User Authentication / Login
 * - Turf CRUD Operations
 * - Slot Scheduling & Batch Generation
 * - Booking Creation & Slot Status Transition (AVAILABLE -> BOOKED)
 * - Strict Duplicate Booking Prevention
 * - Booking Cancellation & Slot Reversion (BOOKED -> AVAILABLE)
 * - Simulated Payment Verification (Amount matching & Transaction ID)
 * - Review & Rating Bounds Validation (1-5)
 * - Customer Complaint & Ticket Resolution Lifecycle
 */
@SpringBootTest
@Transactional
class SmartTurfBookingApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private TurfService turfService;

    @Autowired
    private TurfSlotService turfSlotService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ComplaintService complaintService;

    private User testCustomer;
    private Turf testTurf;
    private TurfSlot testSlot;

    @BeforeEach
    void setUp() {
        // Create base customer for tests
        RegisterRequest registerReq = new RegisterRequest(
                "Test Player",
                "testplayer" + System.currentTimeMillis() + "@gmail.com",
                "password123",
                "9876500000"
        );
        testCustomer = userService.register(registerReq);

        // Create base turf
        Turf turf = new Turf(
                null,
                "Test Arena",
                "Andheri West, Mumbai",
                "High quality test arena",
                new BigDecimal("1000.00"),
                "Football",
                14,
                "https://images.unsplash.com/photo-test",
                "AVAILABLE"
        );
        testTurf = turfService.createTurf(turf);

        // Create base slot
        SlotRequest slotReq = new SlotRequest(
                testTurf.getId(),
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "AVAILABLE"
        );
        testSlot = turfSlotService.createSlot(slotReq);
    }

    @Test
    @DisplayName("1. Test User Registration and Duplicate Email Rejection")
    void testUserRegistrationAndUniqueEmail() {
        assertNotNull(testCustomer.getId());
        assertEquals("CUSTOMER", testCustomer.getRole());

        // Attempt duplicate email registration -> Expect BookingException
        RegisterRequest duplicateReq = new RegisterRequest(
                "Another Player",
                testCustomer.getEmail(),
                "password456",
                "9876511111"
        );

        assertThrows(BookingException.class, () -> userService.register(duplicateReq));
    }

    @Test
    @DisplayName("2. Test User Login Authentication")
    void testUserLogin() {
        // Valid login
        LoginRequest validLogin = new LoginRequest(testCustomer.getEmail(), "password123");
        User loggedIn = userService.login(validLogin);
        assertNotNull(loggedIn);
        assertEquals(testCustomer.getId(), loggedIn.getId());

        // Invalid password
        LoginRequest invalidLogin = new LoginRequest(testCustomer.getEmail(), "wrongpass");
        assertThrows(BookingException.class, () -> userService.login(invalidLogin));
    }

    @Test
    @DisplayName("3. Test Turf CRUD Operations")
    void testTurfCRUD() {
        // Read
        Turf found = turfService.getTurfById(testTurf.getId());
        assertEquals("Test Arena", found.getName());

        // Update
        found.setName("Updated Arena Name");
        Turf updated = turfService.updateTurf(found.getId(), found);
        assertEquals("Updated Arena Name", updated.getName());

        // Search
        List<Turf> searchResults = turfService.searchTurfs("Updated");
        assertFalse(searchResults.isEmpty());
    }

    @Test
    @DisplayName("4. Test Slot Scheduling & Batch Generation")
    void testSlotScheduling() {
        assertNotNull(testSlot.getId());
        assertEquals("AVAILABLE", testSlot.getStatus());

        // Batch generation for 2 hours (14:00 to 16:00)
        List<TurfSlot> generated = turfSlotService.generateDailySlots(
                testTurf.getId(),
                LocalDate.now().plusDays(2),
                14,
                16
        );
        assertEquals(2, generated.size());
    }

    @Test
    @DisplayName("5. Test Booking Creation & Status Transition (AVAILABLE -> BOOKED)")
    void testBookingCreation() {
        BookingRequest bookingReq = new BookingRequest(
                testCustomer.getId(),
                testTurf.getId(),
                testSlot.getId(),
                testSlot.getSlotDate(),
                new BigDecimal("1000.00")
        );

        Booking booking = bookingService.createBooking(bookingReq);
        assertNotNull(booking.getId());
        assertEquals("PENDING", booking.getStatus());

        // Verify slot status changed to BOOKED
        TurfSlot updatedSlot = turfSlotService.getSlotById(testSlot.getId());
        assertEquals("BOOKED", updatedSlot.getStatus());
    }

    @Test
    @DisplayName("6. Test Strict Duplicate Booking Prevention")
    void testDuplicateBookingPrevention() {
        // First booking succeeds
        BookingRequest firstReq = new BookingRequest(
                testCustomer.getId(),
                testTurf.getId(),
                testSlot.getId(),
                testSlot.getSlotDate(),
                new BigDecimal("1000.00")
        );
        bookingService.createBooking(firstReq);

        // Second booking on the same slot must fail with BookingException
        BookingRequest secondReq = new BookingRequest(
                testCustomer.getId(),
                testTurf.getId(),
                testSlot.getId(),
                testSlot.getSlotDate(),
                new BigDecimal("1000.00")
        );

        BookingException exception = assertThrows(BookingException.class, () -> {
            bookingService.createBooking(secondReq);
        });
        assertTrue(exception.getMessage().contains("already booked"));
    }

    @Test
    @DisplayName("7. Test Booking Cancellation & Slot Reversion (BOOKED -> AVAILABLE)")
    void testBookingCancellation() {
        BookingRequest bookingReq = new BookingRequest(
                testCustomer.getId(),
                testTurf.getId(),
                testSlot.getId(),
                testSlot.getSlotDate(),
                new BigDecimal("1000.00")
        );
        Booking booking = bookingService.createBooking(bookingReq);

        // Cancel the booking
        Booking cancelled = bookingService.cancelBooking(booking.getId());
        assertEquals("CANCELLED", cancelled.getStatus());

        // Verify slot was released back to AVAILABLE
        TurfSlot freedSlot = turfSlotService.getSlotById(testSlot.getId());
        assertEquals("AVAILABLE", freedSlot.getStatus());
    }

    @Test
    @DisplayName("8. Test Simulated Payment Processing")
    void testPaymentProcessing() {
        BookingRequest bookingReq = new BookingRequest(
                testCustomer.getId(),
                testTurf.getId(),
                testSlot.getId(),
                testSlot.getSlotDate(),
                new BigDecimal("1000.00")
        );
        Booking booking = bookingService.createBooking(bookingReq);

        // Process simulated payment
        PaymentRequest paymentReq = new PaymentRequest(
                booking.getId(),
                new BigDecimal("1000.00"),
                "UPI"
        );
        Payment payment = paymentService.processPayment(paymentReq);

        assertNotNull(payment.getId());
        assertEquals("SUCCESS", payment.getPaymentStatus());
        assertTrue(payment.getTransactionId().startsWith("TXN"));

        // Verify booking status transitioned to CONFIRMED
        Booking confirmedBooking = bookingService.getBookingById(booking.getId());
        assertEquals("CONFIRMED", confirmedBooking.getStatus());
    }

    @Test
    @DisplayName("9. Test Review & Rating Bounds Validation (1 to 5)")
    void testReviewCreationAndBounds() {
        ReviewRequest validReview = new ReviewRequest(
                testCustomer.getId(),
                testTurf.getId(),
                5,
                "Outstanding turf quality and lighting!"
        );
        Review review = reviewService.addReview(validReview);
        assertNotNull(review.getId());
        assertEquals(5, review.getRating());

        // Test invalid rating < 1
        ReviewRequest invalidReviewLow = new ReviewRequest(
                testCustomer.getId(),
                testTurf.getId(),
                0,
                "Invalid rating"
        );
        assertThrows(BookingException.class, () -> reviewService.addReview(invalidReviewLow));

        // Test invalid rating > 5
        ReviewRequest invalidReviewHigh = new ReviewRequest(
                testCustomer.getId(),
                testTurf.getId(),
                6,
                "Invalid rating"
        );
        assertThrows(BookingException.class, () -> reviewService.addReview(invalidReviewHigh));
    }

    @Test
    @DisplayName("10. Test Complaint Ticket Lifecycle")
    void testComplaintTicketLifecycle() {
        ComplaintRequest complaintReq = new ComplaintRequest(
                testCustomer.getId(),
                "Water cooler issue",
                "Water cooler not working in locker room 2"
        );
        Complaint complaint = complaintService.createComplaint(complaintReq);
        assertNotNull(complaint.getId());
        assertEquals("OPEN", complaint.getStatus());

        // Admin updates status to IN_PROGRESS then RESOLVED
        Complaint inProgress = complaintService.updateComplaintStatus(complaint.getId(), "IN_PROGRESS");
        assertEquals("IN_PROGRESS", inProgress.getStatus());

        Complaint resolved = complaintService.updateComplaintStatus(complaint.getId(), "RESOLVED");
        assertEquals("RESOLVED", resolved.getStatus());
    }
}
