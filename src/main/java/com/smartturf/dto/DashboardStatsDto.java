package com.smartturf.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Admin Dashboard Statistics.
 */
public class DashboardStatsDto {

    private long totalUsers;
    private long totalCustomers;
    private long totalTurfs;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;
    private long pendingComplaints;
    private long totalReviews;

    public DashboardStatsDto() {
        this.totalRevenue = BigDecimal.ZERO;
    }

    public DashboardStatsDto(long totalUsers, long totalCustomers, long totalTurfs, long totalBookings,
                             long confirmedBookings, long cancelledBookings, BigDecimal totalRevenue,
                             long pendingComplaints, long totalReviews) {
        this.totalUsers = totalUsers;
        this.totalCustomers = totalCustomers;
        this.totalTurfs = totalTurfs;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.pendingComplaints = pendingComplaints;
        this.totalReviews = totalReviews;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalTurfs() {
        return totalTurfs;
    }

    public void setTotalTurfs(long totalTurfs) {
        this.totalTurfs = totalTurfs;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getPendingComplaints() {
        return pendingComplaints;
    }

    public void setPendingComplaints(long pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
