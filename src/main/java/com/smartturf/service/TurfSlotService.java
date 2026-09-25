package com.smartturf.service;

import com.smartturf.dto.SlotRequest;
import com.smartturf.entity.Turf;
import com.smartturf.entity.TurfSlot;
import com.smartturf.exception.BookingException;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.TurfRepository;
import com.smartturf.repository.TurfSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing turf scheduling slots and availability.
 */
@Service
@Transactional
public class TurfSlotService {

    private final TurfSlotRepository turfSlotRepository;
    private final TurfRepository turfRepository;

    @Autowired
    public TurfSlotService(TurfSlotRepository turfSlotRepository, TurfRepository turfRepository) {
        this.turfSlotRepository = turfSlotRepository;
        this.turfRepository = turfRepository;
    }

    // Get all slots
    @Transactional(readOnly = true)
    public List<TurfSlot> getAllSlots() {
        return turfSlotRepository.findAll();
    }

    // Get slot by ID
    @Transactional(readOnly = true)
    public TurfSlot getSlotById(Long id) {
        return turfSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + id));
    }

    // Get all slots for a turf
    @Transactional(readOnly = true)
    public List<TurfSlot> getSlotsByTurf(Long turfId) {
        return turfSlotRepository.findByTurfId(turfId);
    }

    // Get slots for a turf on a specific date
    @Transactional(readOnly = true)
    public List<TurfSlot> getSlotsByTurfAndDate(Long turfId, LocalDate date) {
        return turfSlotRepository.findByTurfIdAndSlotDateOrderByStartTimeAsc(turfId, date);
    }

    // Get only available slots for booking
    @Transactional(readOnly = true)
    public List<TurfSlot> getAvailableSlots(Long turfId, LocalDate date) {
        return turfSlotRepository.findByTurfIdAndSlotDateAndStatusOrderByStartTimeAsc(turfId, date, "AVAILABLE");
    }

    // Create a new slot
    public TurfSlot createSlot(SlotRequest request) {
        Turf turf = turfRepository.findById(request.getTurfId())
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + request.getTurfId()));

        // Prevent duplicate slot on the same turf, date, and start time
        if (turfSlotRepository.existsByTurfIdAndSlotDateAndStartTime(
                request.getTurfId(), request.getSlotDate(), request.getStartTime())) {
            throw new BookingException("A slot already exists for this turf at " + request.getStartTime() + " on " + request.getSlotDate());
        }

        TurfSlot slot = new TurfSlot();
        slot.setTurf(turf);
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");

        return turfSlotRepository.save(slot);
    }

    // Batch generate hourly slots for a date (e.g. 06:00 to 22:00)
    public List<TurfSlot> generateDailySlots(Long turfId, LocalDate date, int startHour, int endHour) {
        Turf turf = turfRepository.findById(turfId)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + turfId));

        List<TurfSlot> createdSlots = new ArrayList<>();
        for (int h = startHour; h < endHour; h++) {
            LocalTime start = LocalTime.of(h, 0);
            LocalTime end = LocalTime.of(h + 1, 0);

            if (!turfSlotRepository.existsByTurfIdAndSlotDateAndStartTime(turfId, date, start)) {
                TurfSlot slot = new TurfSlot(null, turf, date, start, end, "AVAILABLE");
                createdSlots.add(turfSlotRepository.save(slot));
            }
        }
        return createdSlots;
    }

    // Update slot
    public TurfSlot updateSlot(Long id, SlotRequest request) {
        TurfSlot slot = getSlotById(id);
        if (!slot.getTurf().getId().equals(request.getTurfId())) {
            Turf turf = turfRepository.findById(request.getTurfId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + request.getTurfId()));
            slot.setTurf(turf);
        }
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        if (request.getStatus() != null) {
            slot.setStatus(request.getStatus());
        }
        return turfSlotRepository.save(slot);
    }

    // Update slot status directly
    public TurfSlot updateSlotStatus(Long id, String status) {
        TurfSlot slot = getSlotById(id);
        slot.setStatus(status);
        return turfSlotRepository.save(slot);
    }

    // Delete slot
    public void deleteSlot(Long id) {
        TurfSlot slot = getSlotById(id);
        turfSlotRepository.delete(slot);
    }
}
