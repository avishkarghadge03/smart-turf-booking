package com.smartturf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * TurfSlot Entity representing an hourly time slot for a specific turf.
 */
@Entity
@Table(name = "turf_slots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_turf_slot", columnNames = {"turf_id", "slot_date", "start_time"})
})
public class TurfSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Turf is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "turf_id", nullable = false)
    private Turf turf;

    @NotNull(message = "Slot date is required")
    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // 'AVAILABLE' or 'BOOKED'

    public TurfSlot() {
    }

    public TurfSlot(Long id, Turf turf, LocalDate slotDate, LocalTime startTime, LocalTime endTime, String status) {
        this.id = id;
        this.turf = turf;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : "AVAILABLE";
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "AVAILABLE";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Turf getTurf() {
        return turf;
    }

    public void setTurf(Turf turf) {
        this.turf = turf;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
