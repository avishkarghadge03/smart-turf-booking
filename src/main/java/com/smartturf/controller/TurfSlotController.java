package com.smartturf.controller;

import com.smartturf.dto.ApiResponse;
import com.smartturf.dto.SlotRequest;
import com.smartturf.entity.TurfSlot;
import com.smartturf.service.TurfSlotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Turf Slots and scheduling.
 */
@RestController
@RequestMapping("/api/slots")
public class TurfSlotController {

    private final TurfSlotService turfSlotService;

    @Autowired
    public TurfSlotController(TurfSlotService turfSlotService) {
        this.turfSlotService = turfSlotService;
    }

    // Get all slots: GET /api/slots
    @GetMapping
    public ResponseEntity<List<TurfSlot>> getAllSlots() {
        return ResponseEntity.ok(turfSlotService.getAllSlots());
    }

    // Get slot by ID: GET /api/slots/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TurfSlot> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(turfSlotService.getSlotById(id));
    }

    // Get slots for a specific turf, optionally filtered by date: GET /api/slots/turf/{turfId}?date=2026-09-09&availableOnly=false
    @GetMapping("/turf/{turfId}")
    public ResponseEntity<List<TurfSlot>> getSlotsByTurf(
            @PathVariable Long turfId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "false") boolean availableOnly) {

        if (date != null) {
            if (availableOnly) {
                return ResponseEntity.ok(turfSlotService.getAvailableSlots(turfId, date));
            } else {
                return ResponseEntity.ok(turfSlotService.getSlotsByTurfAndDate(turfId, date));
            }
        }
        return ResponseEntity.ok(turfSlotService.getSlotsByTurf(turfId));
    }

    // Create a new slot: POST /api/slots
    @PostMapping
    public ResponseEntity<ApiResponse<TurfSlot>> createSlot(@Valid @RequestBody SlotRequest request) {
        TurfSlot created = turfSlotService.createSlot(request);
        return new ResponseEntity<>(
                ApiResponse.ok("Slot created successfully!", created),
                HttpStatus.CREATED
        );
    }

    // Batch generate hourly slots for a date: POST /api/slots/generate?turfId=1&date=2026-09-09&startHour=6&endHour=22
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<TurfSlot>>> generateDailySlots(
            @RequestParam Long turfId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "6") int startHour,
            @RequestParam(defaultValue = "22") int endHour) {

        List<TurfSlot> generated = turfSlotService.generateDailySlots(turfId, date, startHour, endHour);
        return new ResponseEntity<>(
                ApiResponse.ok("Generated " + generated.size() + " slots for date: " + date, generated),
                HttpStatus.CREATED
        );
    }

    // Update slot: PUT /api/slots/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TurfSlot>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody SlotRequest request) {
        TurfSlot updated = turfSlotService.updateSlot(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Slot updated successfully!", updated));
    }

    // Delete slot: DELETE /api/slots/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable Long id) {
        turfSlotService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.ok("Slot deleted successfully!"));
    }
}
