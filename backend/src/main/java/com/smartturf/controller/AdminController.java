package com.smartturf.controller;

import com.smartturf.dto.DashboardStatsDto;
import com.smartturf.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

/**
 * REST Controller for Admin Analytics and Dashboard Metrics.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TurfRepository turfRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final com.smartturf.service.UserService userService;

    @Autowired
    public AdminController(UserRepository userRepository,
                           TurfRepository turfRepository,
                           BookingRepository bookingRepository,
                           PaymentRepository paymentRepository,
                           ComplaintRepository complaintRepository,
                           ReviewRepository reviewRepository,
                           com.smartturf.service.UserService userService) {
        this.userRepository = userRepository;
        this.turfRepository = turfRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.complaintRepository = complaintRepository;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }

    // Admin Registration: POST /api/admin/register
    @org.springframework.web.bind.annotation.PostMapping("/register")
    public ResponseEntity<com.smartturf.dto.ApiResponse<com.smartturf.entity.User>> registerAdmin(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.smartturf.dto.RegisterRequest request) {
        // Enforce ADMIN role
        request.setRole("ADMIN");
        com.smartturf.entity.User registered = userService.register(request);
        return new ResponseEntity<>(
                com.smartturf.dto.ApiResponse.ok("Administrator registered successfully!", registered),
                org.springframework.http.HttpStatus.CREATED
        );
    }

    // Admin Login: POST /api/admin/login
    @org.springframework.web.bind.annotation.PostMapping("/login")
    public ResponseEntity<com.smartturf.dto.ApiResponse<com.smartturf.entity.User>> loginAdmin(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.smartturf.dto.LoginRequest request) {
        com.smartturf.entity.User user = userService.login(request);
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new com.smartturf.exception.BookingException("Access Denied: Account is not an administrator.");
        }
        return ResponseEntity.ok(com.smartturf.dto.ApiResponse.ok("Administrator login successful!", user));
    }

    // Get overview statistics for Admin Dashboard: GET /api/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole("CUSTOMER");
        long totalTurfs = turfRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus("CONFIRMED");
        long cancelledBookings = bookingRepository.countByStatus("CANCELLED");

        BigDecimal revenue = paymentRepository.calculateTotalSuccessfulRevenue();
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        long pendingComplaints = complaintRepository.countByStatus("OPEN");
        long totalReviews = reviewRepository.count();

        DashboardStatsDto stats = new DashboardStatsDto(
                totalUsers,
                totalCustomers,
                totalTurfs,
                totalBookings,
                confirmedBookings,
                cancelledBookings,
                revenue,
                pendingComplaints,
                totalReviews
        );

        return ResponseEntity.ok(stats);
    }
}
