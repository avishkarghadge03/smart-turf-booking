package com.smartturf.service;

import com.smartturf.dto.ComplaintRequest;
import com.smartturf.entity.Complaint;
import com.smartturf.entity.User;
import com.smartturf.exception.ResourceNotFoundException;
import com.smartturf.repository.ComplaintRepository;
import com.smartturf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service managing customer complaints, grievance tickets, and status resolutions.
 */
@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // Submit a new complaint
    public Complaint createComplaint(ComplaintRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setSubject(request.getSubject().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setStatus("OPEN");

        Complaint saved = complaintRepository.save(complaint);

        // Send confirmation notification
        notificationService.createNotification(
                user,
                "Your complaint ticket #" + saved.getId() + " ('" + saved.getSubject() + "') has been received.",
                "COMPLAINT"
        );

        return saved;
    }

    // Get complaints by user
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByUser(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Get all complaints for Admin
    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get complaint by ID
    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
    }

    // Update complaint status (OPEN -> IN_PROGRESS -> RESOLVED) and notify customer
    public Complaint updateComplaintStatus(Long id, String status) {
        Complaint complaint = getComplaintById(id);
        complaint.setStatus(status.toUpperCase());
        Complaint updated = complaintRepository.save(complaint);

        // Notify user about status update
        notificationService.createNotification(
                complaint.getUser(),
                "Your complaint ticket #" + complaint.getId() + " status has been updated to: " + status.toUpperCase(),
                "COMPLAINT"
        );

        return updated;
    }

    // Delete complaint
    public void deleteComplaint(Long id) {
        if (!complaintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Complaint not found with ID: " + id);
        }
        complaintRepository.deleteById(id);
    }
}
