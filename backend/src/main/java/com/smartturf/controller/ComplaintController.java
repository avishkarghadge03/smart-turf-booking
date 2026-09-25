package com.smartturf.controller;

import com.smartturf.dto.ApiResponse;
import com.smartturf.dto.ComplaintRequest;
import com.smartturf.entity.Complaint;
import com.smartturf.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Customer Complaints and Grievance Management.
 */
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    @Autowired
    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // Submit a complaint: POST /api/complaints
    @PostMapping
    public ResponseEntity<ApiResponse<Complaint>> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        Complaint created = complaintService.createComplaint(request);
        return new ResponseEntity<>(
                ApiResponse.ok("Complaint ticket submitted successfully!", created),
                HttpStatus.CREATED
        );
    }

    // Get all complaints (Admin): GET /api/complaints
    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    // Get complaint by ID: GET /api/complaints/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    // Get complaints for a user: GET /api/complaints/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Complaint>> getComplaintsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(complaintService.getComplaintsByUser(userId));
    }

    // Update complaint status (Admin): PUT /api/complaints/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Complaint>> updateComplaintStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null || status.isBlank()) {
            status = "RESOLVED";
        }
        Complaint updated = complaintService.updateComplaintStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Complaint status updated to: " + status, updated));
    }

    // Delete complaint: DELETE /api/complaints/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.ok("Complaint deleted successfully!"));
    }
}
