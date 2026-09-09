package com.smartturf.controller;

import com.smartturf.dto.ApiResponse;
import com.smartturf.entity.Turf;
import com.smartturf.service.TurfService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Turf management, browsing, and searching.
 */
@RestController
@RequestMapping("/api/turfs")
public class TurfController {

    private final TurfService turfService;

    @Autowired
    public TurfController(TurfService turfService) {
        this.turfService = turfService;
    }

    // Get all turfs or search: GET /api/turfs?search=mumbai
    @GetMapping
    public ResponseEntity<List<Turf>> getTurfs(@RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(turfService.searchTurfs(search));
        }
        return ResponseEntity.ok(turfService.getAllTurfs());
    }

    // Get turf by ID: GET /api/turfs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Turf> getTurfById(@PathVariable Long id) {
        return ResponseEntity.ok(turfService.getTurfById(id));
    }

    // Create a new turf: POST /api/turfs
    @PostMapping
    public ResponseEntity<ApiResponse<Turf>> createTurf(@Valid @RequestBody Turf turf) {
        Turf created = turfService.createTurf(turf);
        return new ResponseEntity<>(
                ApiResponse.ok("Turf created successfully!", created),
                HttpStatus.CREATED
        );
    }

    // Update turf: PUT /api/turfs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Turf>> updateTurf(@PathVariable Long id, @Valid @RequestBody Turf turf) {
        Turf updated = turfService.updateTurf(id, turf);
        return ResponseEntity.ok(ApiResponse.ok("Turf updated successfully!", updated));
    }

    // Delete turf: DELETE /api/turfs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTurf(@PathVariable Long id) {
        turfService.deleteTurf(id);
        return ResponseEntity.ok(ApiResponse.ok("Turf deleted/deactivated successfully!"));
    }
}
