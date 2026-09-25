/**
 * Smart Turf Booking & Management System
 * Global API Configuration & Common Utility Functions
 */

const API_BASE_URL = "http://localhost:8080/api";

// Global time formatter: converts "07:00:00" to "7:00 AM"
function formatTime(timeStr) {
  if (!timeStr) return "";
  try {
    const parts = timeStr.split(":");
    if (parts.length < 2) return timeStr;
    let hour = parseInt(parts[0], 10);
    const minute = parts[1] || "00";
    const ampm = hour >= 12 ? "PM" : "AM";
    hour = hour % 12;
    hour = hour ? hour : 12; // 0 hour should be 12
    return `${hour}:${minute} ${ampm}`;
  } catch (e) {
    return timeStr || "";
  }
}
