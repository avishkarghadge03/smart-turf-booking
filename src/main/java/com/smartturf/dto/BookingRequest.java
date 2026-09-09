package com.smartturf.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating a turf reservation.
 */
public class BookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Turf ID is required")
    private Long turfId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    private BigDecimal totalAmount;

    public BookingRequest() {
    }

    public BookingRequest(Long userId, Long turfId, Long slotId, LocalDate bookingDate, BigDecimal totalAmount) {
        this.userId = userId;
        this.turfId = turfId;
        this.slotId = slotId;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTurfId() {
        return turfId;
    }

    public void setTurfId(Long turfId) {
        this.turfId = turfId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
