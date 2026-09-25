package com.smartturf.service;

import com.smartturf.entity.Turf;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.BookingRepository;
import com.smartturf.repository.TurfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service managing Turf arenas, search, and availability.
 */
@Service
@Transactional
public class TurfService {

    private final TurfRepository turfRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public TurfService(TurfRepository turfRepository, BookingRepository bookingRepository) {
        this.turfRepository = turfRepository;
        this.bookingRepository = bookingRepository;
    }

    // Get all turfs
    @Transactional(readOnly = true)
    public List<Turf> getAllTurfs() {
        return turfRepository.findAll();
    }

    // Get only available turfs
    @Transactional(readOnly = true)
    public List<Turf> getAvailableTurfs() {
        return turfRepository.findByStatus("AVAILABLE");
    }

    // Get turf by ID
    @Transactional(readOnly = true)
    public Turf getTurfById(Long id) {
        return turfRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with ID: " + id));
    }

    // Search turfs by keyword
    @Transactional(readOnly = true)
    public List<Turf> searchTurfs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return turfRepository.findAll();
        }
        return turfRepository.searchTurfs(query.trim());
    }

    // Create a new turf
    public Turf createTurf(Turf turf) {
        if (turf.getStatus() == null || turf.getStatus().isBlank()) {
            turf.setStatus("AVAILABLE");
        }
        return turfRepository.save(turf);
    }

    // Update an existing turf
    public Turf updateTurf(Long id, Turf turfDetails) {
        Turf existing = getTurfById(id);
        existing.setName(turfDetails.getName().trim());
        existing.setLocation(turfDetails.getLocation().trim());
        existing.setDescription(turfDetails.getDescription());
        existing.setPricePerHour(turfDetails.getPricePerHour());
        existing.setSportType(turfDetails.getSportType().trim());
        existing.setCapacity(turfDetails.getCapacity());
        existing.setImageUrl(turfDetails.getImageUrl());
        existing.setStatus(turfDetails.getStatus());
        return turfRepository.save(existing);
    }

    // Delete turf safely (Rule 10: handle relationships safely)
    public void deleteTurf(Long id) {
        Turf turf = getTurfById(id);
        long bookingCount = bookingRepository.findByTurfIdOrderByCreatedAtDesc(id).size();
        if (bookingCount > 0) {
            // Soft delete / disable turf so historical bookings remain intact
            turf.setStatus("UNAVAILABLE");
            turfRepository.save(turf);
        } else {
            turfRepository.delete(turf);
        }
    }
}
