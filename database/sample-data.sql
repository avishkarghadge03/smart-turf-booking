-- ==========================================================
-- Smart Turf Booking & Management System
-- Sample Seed Data (MySQL 8.x)
-- ==========================================================

USE smart_turf_booking_system;

-- 1. INSERT USERS (1 Admin + 5 Customers)
INSERT INTO users (id, name, email, password, phone, role, created_at) VALUES
(1, 'Admin User', 'admin@smartturf.com', 'admin123', '9876543210', 'ADMIN', NOW()),
(2, 'Rahul Sharma', 'rahul@gmail.com', 'rahul123', '9820112233', 'CUSTOMER', NOW()),
(3, 'Priya Patel', 'priya@gmail.com', 'priya123', '9820223344', 'CUSTOMER', NOW()),
(4, 'Amit Verma', 'amit@gmail.com', 'amit123', '9820334455', 'CUSTOMER', NOW()),
(5, 'Sneha Kulkarni', 'sneha@gmail.com', 'sneha123', '9820445566', 'CUSTOMER', NOW()),
(6, 'Vikram Singh', 'vikram@gmail.com', 'vikram123', '9820556677', 'CUSTOMER', NOW());

-- 2. INSERT TURFS (5 Realistic Sports Turfs)
INSERT INTO turfs (id, name, location, description, price_per_hour, sport_type, capacity, image_url, status, created_at) VALUES
(1, 'KickOff Football & Box Cricket Arena', 'Andheri West, Mumbai, Maharashtra', 'Premium FIFA-grade artificial grass turf with LED floodlights, sound system, changing rooms, and spectator seating. Perfect for 7v7 football and box cricket.', 1200.00, 'Football & Cricket', 14, 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=1000&q=80', 'AVAILABLE', NOW()),
(2, 'GreenField Multi-Sport Turf', 'Kothrud, Pune, Maharashtra', 'High-density lush green astro turf equipped with safety netting, clean drinking water, equipment rental, and ample parking. Best for 6v6 matches.', 900.00, 'Football & Cricket', 12, 'https://images.unsplash.com/photo-1459865264687-595d652de67e?auto=format&fit=crop&w=1000&q=80', 'AVAILABLE', NOW()),
(3, 'Thunder Arena Sports Complex', 'Indiranagar, Bangalore, Karnataka', 'State-of-the-art all-weather multi-sport facility with night tournament lighting, air-conditioned locker rooms, cafeteria, and live match scoring board.', 1500.00, 'Multi-Sport', 16, 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1000&q=80', 'AVAILABLE', NOW()),
(4, 'Metro AstroTurf Arena', 'Gachibowli, Hyderabad, Telangana', 'Centrally located premium turf court suitable for football, box cricket, and volleyball. Features top shock-absorption underlay and pristine night lights.', 800.00, 'Football', 10, 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1000&q=80', 'AVAILABLE', NOW()),
(5, 'Champions Soccer & Turf Park', 'Dwarka Sector 12, New Delhi', 'Spacious dual-pitch arena offering European imported turf fibers, high-definition match recording cameras, and tournament hosting facilities.', 1100.00, 'Football & Cricket', 14, 'https://images.unsplash.com/photo-1518091043644-c1d4457512c6?auto=format&fit=crop&w=1000&q=80', 'AVAILABLE', NOW());

-- 3. INSERT TURF SLOTS
-- Turf 1 Slots (Today & Tomorrow)
INSERT INTO turf_slots (id, turf_id, slot_date, start_time, end_time, status) VALUES
(1, 1, CURDATE(), '06:00:00', '07:00:00', 'BOOKED'),
(2, 1, CURDATE(), '07:00:00', '08:00:00', 'AVAILABLE'),
(3, 1, CURDATE(), '08:00:00', '09:00:00', 'AVAILABLE'),
(4, 1, CURDATE(), '17:00:00', '18:00:00', 'BOOKED'),
(5, 1, CURDATE(), '18:00:00', '19:00:00', 'AVAILABLE'),
(6, 1, CURDATE(), '19:00:00', '20:00:00', 'AVAILABLE'),
(7, 1, CURDATE(), '20:00:00', '21:00:00', 'AVAILABLE'),
(8, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '06:00:00', '07:00:00', 'AVAILABLE'),
(9, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '08:00:00', 'AVAILABLE'),
(10, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', '19:00:00', 'AVAILABLE'),
(11, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', '20:00:00', 'AVAILABLE'),
(12, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '20:00:00', '21:00:00', 'AVAILABLE');

-- Turf 2 Slots
INSERT INTO turf_slots (id, turf_id, slot_date, start_time, end_time, status) VALUES
(13, 2, CURDATE(), '07:00:00', '08:00:00', 'AVAILABLE'),
(14, 2, CURDATE(), '08:00:00', '09:00:00', 'BOOKED'),
(15, 2, CURDATE(), '17:00:00', '18:00:00', 'AVAILABLE'),
(16, 2, CURDATE(), '18:00:00', '19:00:00', 'AVAILABLE'),
(17, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '08:00:00', 'AVAILABLE'),
(18, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', '19:00:00', 'AVAILABLE');

-- Turf 3 Slots
INSERT INTO turf_slots (id, turf_id, slot_date, start_time, end_time, status) VALUES
(19, 3, CURDATE(), '06:00:00', '07:00:00', 'AVAILABLE'),
(20, 3, CURDATE(), '19:00:00', '20:00:00', 'BOOKED'),
(21, 3, CURDATE(), '20:00:00', '21:00:00', 'AVAILABLE'),
(22, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', '20:00:00', 'AVAILABLE');

-- Turf 4 Slots
INSERT INTO turf_slots (id, turf_id, slot_date, start_time, end_time, status) VALUES
(23, 4, CURDATE(), '07:00:00', '08:00:00', 'AVAILABLE'),
(24, 4, CURDATE(), '18:00:00', '19:00:00', 'AVAILABLE'),
(25, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', '19:00:00', 'AVAILABLE');

-- Turf 5 Slots
INSERT INTO turf_slots (id, turf_id, slot_date, start_time, end_time, status) VALUES
(26, 5, CURDATE(), '06:00:00', '07:00:00', 'AVAILABLE'),
(27, 5, CURDATE(), '19:00:00', '20:00:00', 'AVAILABLE'),
(28, 5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', '20:00:00', 'AVAILABLE');

-- 4. INSERT SAMPLE BOOKINGS
INSERT INTO bookings (id, user_id, turf_id, slot_id, booking_date, total_amount, status, created_at) VALUES
(1, 2, 1, 1, CURDATE(), 1200.00, 'CONFIRMED', NOW()),
(2, 3, 1, 4, CURDATE(), 1200.00, 'CONFIRMED', NOW()),
(3, 4, 2, 14, CURDATE(), 900.00, 'CONFIRMED', NOW()),
(4, 5, 3, 20, CURDATE(), 1500.00, 'CONFIRMED', NOW());

-- 5. INSERT SAMPLE PAYMENTS
INSERT INTO payments (id, booking_id, amount, payment_method, transaction_id, payment_status, payment_date) VALUES
(1, 1, 1200.00, 'UPI', 'TXN9810237461', 'SUCCESS', NOW()),
(2, 2, 1200.00, 'CARD', 'TXN9810237462', 'SUCCESS', NOW()),
(3, 3, 900.00, 'UPI', 'TXN9810237463', 'SUCCESS', NOW()),
(4, 4, 1500.00, 'CASH', 'TXN9810237464', 'SUCCESS', NOW());

-- 6. INSERT SAMPLE REVIEWS
INSERT INTO reviews (id, user_id, turf_id, rating, comment, created_at) VALUES
(1, 2, 1, 5, 'Exceptional quality artificial turf! The floodlights are bright and the grip is superb for 7-a-side football.', NOW()),
(2, 3, 1, 4, 'Very good experience playing cricket here. Ample parking space and clean locker rooms.', NOW()),
(3, 4, 2, 5, 'Best budget turf in Kothrud! The netting is high and the staff is polite and helpful.', NOW()),
(4, 5, 3, 5, 'World class facilities. The cafeteria and live scoring display made our weekend tournament amazing!', NOW()),
(5, 6, 4, 4, 'Great location in Gachibowli. Grass quality is soft and easy on the knees.', NOW());

-- 7. INSERT SAMPLE COMPLAINTS
INSERT INTO complaints (id, user_id, subject, description, status, created_at) VALUES
(1, 2, 'Drinking water dispenser empty', 'During our 7 PM game at KickOff Arena, the cold drinking water dispenser was empty. Kindly ensure refills before evening slots.', 'RESOLVED', NOW()),
(2, 4, 'One corner floodlight flickering', 'At GreenField Turf, the northeast corner floodlight was flickering slightly during yesterday evening match.', 'IN_PROGRESS', NOW()),
(3, 5, 'Locker room lock issue', 'Locker number 4 in the men changing room was jammed and could not be locked.', 'OPEN', NOW());

-- 8. INSERT SAMPLE NOTIFICATIONS
INSERT INTO notifications (id, user_id, message, type, is_read, created_at) VALUES
(1, 2, 'Welcome to Smart Turf! Your registration was successful.', 'INFO', TRUE, NOW()),
(2, 2, 'Booking #1 for KickOff Arena has been confirmed. Payment of ₹1,200 received via UPI.', 'BOOKING', TRUE, NOW()),
(3, 3, 'Booking #2 for KickOff Arena has been confirmed. Payment of ₹1,200 received via CARD.', 'BOOKING', FALSE, NOW()),
(4, 4, 'Your complaint regarding floodlight flickering is now IN_PROGRESS.', 'COMPLAINT', FALSE, NOW()),
(5, 5, 'Booking #4 for Thunder Arena has been confirmed. Payment of ₹1,500 recorded as CASH.', 'BOOKING', FALSE, NOW());
