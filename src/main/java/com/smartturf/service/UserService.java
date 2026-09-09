package com.smartturf.service;

import com.smartturf.dto.LoginRequest;
import com.smartturf.dto.RegisterRequest;
import com.smartturf.entity.User;
import com.smartturf.exception.BookingException;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service managing User registration, authentication, and administration.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // Register a new customer
    public User register(RegisterRequest request) {
        // Business Rule 1: Email must be unique
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BookingException("Email address is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone().trim());
        user.setRole("CUSTOMER");

        User savedUser = userRepository.save(user);

        // Send welcome notification
        notificationService.createNotification(
                savedUser,
                "Welcome to Smart Turf, " + savedUser.getName() + "! Your account has been registered successfully.",
                "INFO"
        );

        return savedUser;
    }

    // Authenticate user login
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BookingException("Invalid email or password"));

        // Validate password (plain text check for final-year project simplicity)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BookingException("Invalid email or password");
        }

        return user;
    }

    // Fetch all users (for Admin)
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Fetch user by ID
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    // Create user by Admin (can be ADMIN or CUSTOMER)
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            throw new BookingException("Email address is already registered: " + user.getEmail());
        }
        user.setEmail(user.getEmail().trim().toLowerCase());
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CUSTOMER");
        }
        return userRepository.save(user);
    }

    // Update user profile
    public User updateUser(Long id, User userDetails) {
        User existing = getUserById(id);

        // If email is changing, check uniqueness
        if (!existing.getEmail().equalsIgnoreCase(userDetails.getEmail().trim())) {
            if (userRepository.existsByEmail(userDetails.getEmail().trim().toLowerCase())) {
                throw new BookingException("Email address is already in use by another account");
            }
            existing.setEmail(userDetails.getEmail().trim().toLowerCase());
        }

        existing.setName(userDetails.getName().trim());
        existing.setPhone(userDetails.getPhone().trim());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            existing.setPassword(userDetails.getPassword());
        }
        if (userDetails.getRole() != null && !userDetails.getRole().isBlank()) {
            existing.setRole(userDetails.getRole());
        }

        return userRepository.save(existing);
    }

    // Delete user
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
