# Smart Turf Booking & Management System

A complete, production-grade, beginner-friendly **Smart Turf Booking & Management System** built with **Java 21, Spring Boot 3.x, Spring Data JPA, MySQL 8.x, and Bootstrap 5**.

This project has been architected specifically for a **BSc IT Final-Year Project**, featuring clean modular separation between backend REST APIs and frontend interfaces, detailed documentation, and viva-ready explanations.

---

## 1. Project Overview

The **Smart Turf Booking & Management System** solves the challenge of finding, scheduling, and reserving sports arena slots (Football, Box Cricket, Badminton, Volleyball). 

### Key Capabilities:
- **Customers**:
  - Register and login with secure profile management.
  - Explore turf arenas with location, sport type, and price filters.
  - View real-time hourly slot availability (Available vs Booked).
  - Reserve slots with **strict duplicate booking prevention**.
  - Simulated payment processing (UPI, Debit/Credit Card, Cash at venue) with unique transaction generation.
  - Access printable digital booking receipts.
  - Cancel bookings with automated slot recovery.
  - Rate and review turf venues (1 to 5 stars).
  - Submit grievance / support complaint tickets.
  - Receive automated in-app notifications on bookings, payments, and ticket updates.
- **Administrators**:
  - Dedicated admin dashboard featuring key metrics: Total Users, Total Turfs, Total Bookings, Confirmed Bookings, Total Revenue, and Pending Complaints.
  - Full CRUD management of Users (Customers & Admins).
  - Full CRUD management of Turf Arenas with images and capacity.
  - Slot scheduling with both single-slot and **1-click batch daily slot generation** (e.g., 06:00 to 23:00).
  - Booking status management and manual cancellation / release.
  - Simulated payment transactions audit ledger.
  - Customer reviews moderation.
  - Customer support complaints ticketing workflow (OPEN &rarr; IN_PROGRESS &rarr; RESOLVED).

---

## 2. Technology Stack

### Backend
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.2.x
- **Modules**:
  - Spring Web (RESTful API architecture)
  - Spring Data JPA (Data persistence & repository abstraction)
  - Hibernate (ORM and schema management)
  - Jakarta Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Email`)
- **Build Tool**: Apache Maven 3.x
- **Database Connector**: MySQL Connector/J 8.x
- **Testing**: JUnit 5, Spring Boot Test, H2 in-memory database

### Frontend
- **Languages**: HTML5, CSS3, JavaScript (ES6+)
- **UI Framework**: Bootstrap 5.3
- **Icons**: Bootstrap Icons 1.11
- **Design Theme**: Custom Emerald Green athletic turf palette (`#10b981` / `#0f172a`)
- **Communication**: Native JavaScript `fetch()` API consuming JSON endpoints

### Database
- **Engine**: MySQL Server 8.x
- **Database Name**: `smart_turf_booking_system`

---

## 3. Project Structure

```text
Smart_turf_booking_System/
│
├── backend/
│   ├── pom.xml                                  # Maven dependencies and build plugin
│   └── src/
│       ├── main/
│       │   ├── java/com/smartturf/
│       │   │   ├── SmartTurfBookingApplication.java   # Spring Boot main entrypoint
│       │   │   │
│       │   │   ├── config/
│       │   │   │   └── CorsConfig.java          # Cross-Origin Resource Sharing (CORS)
│       │   │   │
│       │   │   ├── entity/                      # JPA Entities (8 Tables)
│       │   │   │   ├── User.java
│       │   │   │   ├── Turf.java
│       │   │   │   ├── TurfSlot.java
│       │   │   │   ├── Booking.java
│       │   │   │   ├── Payment.java
│       │   │   │   ├── Review.java
│       │   │   │   ├── Complaint.java
│       │   │   │   └── Notification.java
│       │   │   │
│       │   │   ├── repository/                  # Spring Data JPA Repositories
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── TurfRepository.java
│       │   │   │   ├── TurfSlotRepository.java
│       │   │   │   ├── BookingRepository.java
│       │   │   │   ├── PaymentRepository.java
│       │   │   │   ├── ReviewRepository.java
│       │   │   │   ├── ComplaintRepository.java
│       │   │   │   └── NotificationRepository.java
│       │   │   │
│       │   │   ├── service/                     # Service Layer (Business Logic)
│       │   │   │   ├── UserService.java
│       │   │   │   ├── TurfService.java
│       │   │   │   ├── TurfSlotService.java
│       │   │   │   ├── BookingService.java
│       │   │   │   ├── PaymentService.java
│       │   │   │   ├── ReviewService.java
│       │   │   │   ├── ComplaintService.java
│       │   │   │   └── NotificationService.java
│       │   │   │
│       │   │   ├── controller/                  # REST Controllers
│       │   │   │   ├── UserController.java
│       │   │   │   ├── TurfController.java
│       │   │   │   ├── TurfSlotController.java
│       │   │   │   ├── BookingController.java
│       │   │   │   ├── PaymentController.java
│       │   │   │   ├── ReviewController.java
│       │   │   │   ├── ComplaintController.java
│       │   │   │   ├── NotificationController.java
│       │   │   │   └── AdminController.java
│       │   │   │
│       │   │   ├── dto/                         # Data Transfer Objects
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── BookingRequest.java
│       │   │   │   ├── PaymentRequest.java
│       │   │   │   ├── ReviewRequest.java
│       │   │   │   ├── ComplaintRequest.java
│       │   │   │   ├── SlotRequest.java
│       │   │   │   ├── DashboardStatsDto.java
│       │   │   │   └── ApiResponse.java
│       │   │   │
│       │   │   └── exception/                   # Global Exception Handling
│       │   │       ├── ResourceNotFoundException.java
│       │   │       ├── BookingException.java
│       │   │       └── GlobalExceptionHandler.java
│       │   │
│       │   └── resources/
│       │       └── application.properties       # DB credentials & JPA configuration
│       │
│       └── test/java/com/smartturf/
│           └── SmartTurfBookingApplicationTests.java  # Comprehensive JUnit 5 Test Suite
│
├── frontend/
│   ├── index.html                               # Landing page with search & featured turfs
│   ├── login.html                               # Authentication with demo autofill
│   ├── register.html                            # Customer account signup
│   ├── dashboard.html                           # Customer dashboard & notifications
│   ├── turfs.html                               # Turf arena exploration & filters
│   ├── turf-details.html                        # Arena specs & interactive slot picker
│   ├── booking.html                             # Reservation summary & confirmation
│   ├── bookings.html                            # Booking history & receipt modal
│   ├── payment.html                             # Simulated payment gateway (UPI/Card/Cash)
│   ├── reviews.html                             # Customer feedback & 5-star ratings
│   ├── complaints.html                          # Grievance ticket submission & status
│   │
│   ├── admin/                                   # Dedicated Administrator Portal
│   │   ├── dashboard.html                       # Metric cards, revenue tally, recent bookings
│   │   ├── users.html                           # User management (CRUD)
│   │   ├── turfs.html                           # Turf listing & status toggle
│   │   ├── turf-form.html                       # Add / Edit turf form
│   │   ├── slots.html                           # Slot scheduling & batch generator
│   │   ├── bookings.html                        # Booking reservations management
│   │   ├── payments.html                        # Payment transactions ledger
│   │   ├── reviews.html                         # Review moderation
│   │   └── complaints.html                      # Grievance status updater
│   │
│   ├── css/
│   │   └── style.css                            # Modern responsive athletic styling
│   │
│   └── js/                                      # Reusable modular JavaScript
│       ├── config.js                            # API_BASE_URL ("http://localhost:8080/api")
│       ├── auth.js                              # Session, route guards, dynamic navbar
│       ├── home.js                              # Landing page actions
│       ├── turf.js                              # Turf browsing & real-time slot selector
│       ├── booking.js                           # Checkout, cancellation & receipt logic
│       ├── payment.js                           # Payment simulation handler
│       ├── review.js                            # Star ratings & review submissions
│       ├── complaint.js                         # Grievance ticket handling
│       └── admin.js                             # Comprehensive admin panel controller
│
├── database/
│   ├── database.sql                             # DDL Schema for 8 tables with constraints
│   └── sample-data.sql                          # Realistic Indian sample seed data
│
└── README.md
```

---

## 4. Database Setup (MySQL 8.x)

### Step 1: Open MySQL Shell or MySQL Workbench
Open MySQL command prompt or terminal:
```bash
mysql -u root -p
```
Enter your MySQL password.

### Step 2: Execute Schema Script
Run the `database.sql` script to create the database and all 8 tables:
```sql
SOURCE d:/Smart_turf_booking_System/database/database.sql;
```
Or copy and paste the contents of `database/database.sql`.

### Step 3: Insert Sample Seed Data
Populate demo users, turfs, slots, bookings, payments, and reviews:
```sql
SOURCE d:/Smart_turf_booking_System/database/sample-data.sql;
```

---

## 5. Backend Configuration & Run

### Step 1: Update MySQL Password
Open file:
`backend/src/main/resources/application.properties`

Update your MySQL root password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_turf_booking_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 2: Compile & Run with Maven
In terminal or PowerShell:
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
The backend server will start on port `8080`:
```text
=================================================
 Smart Turf Booking System Backend is Running!
 REST API Base URL: http://localhost:8080/api
=================================================
```

---

## 6. Frontend Execution

> [!IMPORTANT]
> Because the frontend makes REST API calls using `fetch()`, run the frontend through an HTTP server (do not double-click to open via `file://`).

### Recommended Options:

#### Option A: VS Code Live Server (Easiest)
1. Open the `Smart_turf_booking_System` folder in VS Code.
2. Right-click `frontend/index.html`.
3. Select **"Open with Live Server"** (usually runs on `http://127.0.0.1:5500/frontend/index.html`).

#### Option B: Python Simple HTTP Server
In a terminal:
```bash
cd frontend
python -m http.server 3000
```
Then visit: `http://localhost:3000` in your browser.

#### Option C: Node.js `npx serve`
```bash
cd frontend
npx serve -l 3000
```
Then visit: `http://localhost:3000`.

---

## 7. Demo Accounts & Credentials

| Role | Name | Email | Password | Phone |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | Admin User | `admin@smartturf.com` | `admin123` | 9876543210 |
| **CUSTOMER** | Rahul Sharma | `rahul@gmail.com` | `rahul123` | 9820112233 |
| **CUSTOMER** | Priya Patel | `priya@gmail.com` | `priya123` | 9820223344 |
| **CUSTOMER** | Amit Verma | `amit@gmail.com` | `amit123` | 9820334455 |
| **CUSTOMER** | Sneha Kulkarni | `sneha@gmail.com` | `sneha123` | 9820445566 |
| **CUSTOMER** | Vikram Singh | `vikram@gmail.com` | `vikram123` | 9820556677 |

> [!TIP]
> The `login.html` page includes one-click **"Customer Demo"** and **"Admin Demo"** autofill buttons for immediate viva demonstration!

---

## 8. Complete Booking Flow

1. **Home (`index.html`)**: Customer explores featured turfs or performs keyword search.
2. **Explore Turfs (`turfs.html`)**: Filter by sport (Football, Cricket, Badminton) and sort by price.
3. **Turf Details (`turf-details.html`)**:
   - Customer views turf dimensions, amenities, grass specs, and customer reviews.
   - Customer selects a match date.
   - The slot grid dynamically fetches slots for that date:
     - Available slots are highlighted in green and clickable.
     - Booked slots are greyed out, struck-through, and disabled.
   - Customer selects a slot and clicks **"Reserve Selected Slot"**.
4. **Checkout (`booking.html`)**:
   - Displays player details, selected arena, date, time slot, and total fee.
   - Clicking **"Proceed to Payment"** triggers `POST /api/bookings`.
   - The backend checks concurrency: if slot is already booked, an error is returned; otherwise slot transitions from `AVAILABLE` &rarr; `BOOKED` atomically.
5. **Payment Simulation (`payment.html`)**:
   - Selects payment method: UPI, Credit/Debit Card, or Cash at Venue.
   - Clicking **"Complete Simulated Payment"** calls `POST /api/payments`.
   - Generates simulated transaction ID (e.g. `TXN9810237461`).
   - Payment status becomes `SUCCESS`, booking status becomes `CONFIRMED`.
   - System alert notification is sent to customer.
6. **Booking History (`bookings.html`)**:
   - Customer can view all active and past bookings.
   - Click **"Receipt"** to view and print clean digital ticket.
   - Click **"Cancel"** to cancel reservation: booking status transitions to `CANCELLED`, and the slot is immediately released back to `AVAILABLE`.
7. **Reviews (`reviews.html`)**: Customer writes a review and selects 1 to 5 stars.

---

## 9. REST API Endpoints Reference

### User Controller (`/api/users`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/users/register` | Register new customer account |
| `POST` | `/api/users/login` | Authenticate customer or admin |
| `GET` | `/api/users` | List all users (Admin) |
| `GET` | `/api/users/{id}` | Get user by ID |
| `POST` | `/api/users` | Create user (Admin) |
| `PUT` | `/api/users/{id}` | Update user profile |
| `DELETE`| `/api/users/{id}` | Delete user |

### Turf Controller (`/api/turfs`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/turfs` | Get all turfs (supports `?search=...`) |
| `GET` | `/api/turfs/{id}` | Get turf details by ID |
| `POST` | `/api/turfs` | Add new turf (Admin) |
| `PUT` | `/api/turfs/{id}` | Update turf details |
| `DELETE`| `/api/turfs/{id}` | Deactivate / delete turf |

### Turf Slot Controller (`/api/slots`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/slots` | Get all slots |
| `GET` | `/api/slots/{id}` | Get slot by ID |
| `GET` | `/api/slots/turf/{turfId}` | Get slots for turf (supports `?date=...&availableOnly=true`) |
| `POST` | `/api/slots` | Create single slot |
| `POST` | `/api/slots/generate` | Batch generate hourly daily slots |
| `PUT` | `/api/slots/{id}` | Update slot time / status |
| `DELETE`| `/api/slots/{id}` | Delete slot |

### Booking Controller (`/api/bookings`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/bookings` | List all bookings (Admin) |
| `GET` | `/api/bookings/{id}` | Get booking by ID |
| `GET` | `/api/bookings/user/{userId}` | Get bookings for customer |
| `POST` | `/api/bookings` | Create reservation (atomic slot lock) |
| `PUT` | `/api/bookings/{id}` | Update booking status |
| `PUT` | `/api/bookings/{id}/cancel` | Cancel booking & release slot |
| `DELETE`| `/api/bookings/{id}` | Delete booking |

### Payment Controller (`/api/payments`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/payments` | Process simulated payment |
| `GET` | `/api/payments` | List all payment transactions |
| `GET` | `/api/payments/{id}` | Get payment by ID |
| `GET` | `/api/payments/booking/{bookingId}` | Get payment for a booking |

### Review Controller (`/api/reviews`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/reviews` | Submit customer review (1-5 stars) |
| `GET` | `/api/reviews` | List all reviews (Admin) |
| `GET` | `/api/reviews/turf/{turfId}` | Get reviews for a turf |
| `GET` | `/api/reviews/turf/{turfId}/average` | Get average star rating |
| `DELETE`| `/api/reviews/{id}` | Remove inappropriate review |

### Complaint Controller (`/api/complaints`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/complaints` | Submit grievance ticket |
| `GET` | `/api/complaints` | List all complaints (Admin) |
| `GET` | `/api/complaints/user/{userId}` | Get complaints for customer |
| `PUT` | `/api/complaints/{id}` | Update status (OPEN &rarr; IN_PROGRESS &rarr; RESOLVED) |
| `DELETE`| `/api/complaints/{id}` | Delete complaint ticket |

### Notification Controller (`/api/notifications`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/notifications/user/{userId}` | Get notifications for customer |
| `GET` | `/api/notifications/user/{userId}/unread` | Get unread notifications |
| `GET` | `/api/notifications/user/{userId}/unread-count` | Get unread counter badge |
| `PUT` | `/api/notifications/{id}/read` | Mark notification as read |
| `DELETE`| `/api/notifications/{id}` | Delete notification |

### Admin Analytics Controller (`/api/admin`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Returns JSON summary metrics (users, turfs, bookings, revenue, pending complaints) |

---

## 10. Automated Testing

To run the full backend test suite:
```bash
cd backend
mvn test
```

The test suite in `SmartTurfBookingApplicationTests.java` validates:
1. User registration & unique email rejection.
2. User authentication (valid vs invalid password).
3. Turf CRUD operations & keyword search.
4. Slot scheduling & batch hourly generation.
5. Booking creation & slot status transition (`AVAILABLE` &rarr; `BOOKED`).
6. **Strict duplicate booking prevention** (rejection of duplicate requests on the same slot).
7. Booking cancellation & slot reversion (`BOOKED` &rarr; `AVAILABLE`).
8. Simulated payment verification (amount check & transaction ID generation).
9. Review submission & rating boundary checks (1 to 5).
10. Customer complaint ticket lifecycle (OPEN &rarr; IN_PROGRESS &rarr; RESOLVED).

---

## 11. Important Business Rules Implemented

- **Rule 1 (Unique Email)**: Registration and user updates enforce unique email constraints via both JPA repository and Jakarta Validation.
- **Rule 2 & 3 (Duplicate Booking Prevention)**: In `BookingService.createBooking()`, the slot status is verified inside an atomic `@Transactional` boundary. If status is `BOOKED`, a `BookingException` is thrown. On successful reservation, the slot status is updated to `BOOKED`.
- **Rule 4 (Cancellation Slot Release)**: When a booking is cancelled, `slot.setStatus("AVAILABLE")` is triggered so other players can immediately book the slot.
- **Rule 5 (Payment Amount Matching)**: The simulated payment rejects any request where the submitted payment amount does not match the booking's `totalAmount`.
- **Rule 6 (Rating Bounds)**: Customer review ratings are strictly bounded between 1 and 5.
- **Rule 7 & 8 (Role Separation)**: Customers can book and manage their own reservations; administrative controls are guarded by `ADMIN` role checks.
- **Rule 10 (Safe Deletion)**: Deleting a turf with active booking history marks its status as `UNAVAILABLE` instead of cascading deletes that destroy historical audit records.
