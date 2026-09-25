-- ==========================================================
-- Smart Turf Booking & Management System
-- Database Schema Definition (MySQL 8.x)
-- ==========================================================

DROP DATABASE IF EXISTS smart_turf_booking_system;
CREATE DATABASE smart_turf_booking_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_turf_booking_system;

-- 1. USERS TABLE
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- 'CUSTOMER' or 'ADMIN'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. TURFS TABLE
CREATE TABLE turfs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    price_per_hour DECIMAL(10, 2) NOT NULL,
    sport_type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE' or 'UNAVAILABLE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_turf_sport (sport_type),
    INDEX idx_turf_status (status),
    INDEX idx_turf_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. TURF SLOTS TABLE
CREATE TABLE turf_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    turf_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE' or 'BOOKED'
    CONSTRAINT fk_slots_turf FOREIGN KEY (turf_id) REFERENCES turfs(id) ON DELETE CASCADE,
    CONSTRAINT uk_turf_slot UNIQUE (turf_id, slot_date, start_time),
    INDEX idx_slot_date (slot_date),
    INDEX idx_slot_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. BOOKINGS TABLE
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    turf_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL UNIQUE,
    booking_date DATE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'CONFIRMED', 'CANCELLED', 'PENDING'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_turf FOREIGN KEY (turf_id) REFERENCES turfs(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id) REFERENCES turf_slots(id) ON DELETE RESTRICT,
    INDEX idx_booking_user (user_id),
    INDEX idx_booking_turf (turf_id),
    INDEX idx_booking_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. PAYMENTS TABLE
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- 'UPI', 'CARD', 'CASH'
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'SUCCESS', 'FAILED', 'PENDING'
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_payment_transaction (transaction_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. REVIEWS TABLE
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    turf_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_turf FOREIGN KEY (turf_id) REFERENCES turfs(id) ON DELETE CASCADE,
    INDEX idx_review_turf (turf_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. COMPLAINTS TABLE
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'IN_PROGRESS', 'RESOLVED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_complaint_user (user_id),
    INDEX idx_complaint_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_unread (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
