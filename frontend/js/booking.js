/**
 * Smart Turf Booking & Management System
 * Booking Confirmation, History, and Cancellation JavaScript
 */

// Safe fallback for formatTime
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

document.addEventListener("DOMContentLoaded", () => {
  if (document.getElementById("booking-checkout-container")) {
    initBookingCheckout();
  } else if (document.getElementById("bookings-history-table")) {
    initBookingHistory();
  }
});

/* -------------------------------------------------------------
 * BOOKING CHECKOUT PAGE (booking.html)
 * ------------------------------------------------------------- */
async function initBookingCheckout() {
  if (!requireAuth("CUSTOMER")) return;

  const user = getCurrentUser();
  if (!user || !user.id) {
    alert("Please log in to continue booking.");
    window.location.href = `login.html?redirect=${encodeURIComponent(window.location.href)}`;
    return;
  }

  const params = new URLSearchParams(window.location.search);
  const turfId = params.get("turfId");
  const slotId = params.get("slotId");
  const dateStr = params.get("date");

  if (!turfId || !slotId) {
    alert("Missing booking parameters. Please choose a turf and slot.");
    window.location.href = "turfs.html";
    return;
  }

  // Pre-fill Customer details
  document.getElementById("checkout-user-name").textContent = user.name || "Customer";
  document.getElementById("checkout-user-email").textContent = user.email || "";
  document.getElementById("checkout-user-phone").textContent = user.phone || "--";

  try {
    // Load turf details
    const turfRes = await fetch(`${API_BASE_URL}/turfs/${turfId}`);
    if (!turfRes.ok) throw new Error("Could not load turf information");
    const turf = await turfRes.json();

    // Load slot details
    const slotRes = await fetch(`${API_BASE_URL}/slots/${slotId}`);
    if (!slotRes.ok) throw new Error("Could not load slot information");
    const slot = await slotRes.json();

    if (slot.status === "BOOKED") {
      alert("Notice: This slot has already been booked by another user. Please choose another slot.");
      window.location.href = `turf-details.html?id=${turfId}`;
      return;
    }

    // Populate checkout fields
    document.getElementById("checkout-turf-name").textContent = turf.name;
    document.getElementById("checkout-turf-location").textContent = turf.location;
    document.getElementById("checkout-turf-sport").textContent = turf.sportType;
    document.getElementById("checkout-turf-img").src = turf.imageUrl || 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=600&q=80';

    document.getElementById("checkout-date").textContent = dateStr || slot.slotDate;
    document.getElementById("checkout-time").textContent = `${formatTime(slot.startTime)} - ${formatTime(slot.endTime)}`;
    document.getElementById("checkout-price").textContent = `₹${turf.pricePerHour}`;
    document.getElementById("checkout-total").textContent = `₹${turf.pricePerHour}`;

    // Handle Confirm Booking Button
    const confirmBtn = document.getElementById("confirm-booking-btn");
    confirmBtn.addEventListener("click", async () => {
      confirmBtn.disabled = true;
      confirmBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Processing Payment...`;

      try {
        const payload = {
          userId: user.id,
          turfId: parseInt(turfId, 10),
          slotId: parseInt(slotId, 10),
          bookingDate: dateStr || slot.slotDate,
          totalAmount: turf.pricePerHour
        };

        // 1. One-click instant checkout (Creates booking & UPI payment together)
        const res = await fetch(`${API_BASE_URL}/bookings`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok) {
          showToast(data.message || "Failed to process payment & booking", "danger");
          confirmBtn.disabled = false;
          confirmBtn.innerHTML = `<i class="bi bi-check-circle-fill me-2"></i>Proceed to Payment`;
          return;
        }

        const checkoutData = data; // Returns the Map from backend

        // 2. Display Confirmed Booked Slot Modal
        document.getElementById("modal-booking-id").textContent = `#${checkoutData.bookingId}`;
        document.getElementById("modal-txn-id").textContent = checkoutData.transactionId;
        document.getElementById("modal-turf-name").textContent = turf.name;
        document.getElementById("modal-match-date").textContent = checkoutData.bookingDate || slot.slotDate;
        document.getElementById("modal-slot-time").textContent = `${formatTime(slot.startTime)} - ${formatTime(slot.endTime)}`;
        document.getElementById("modal-total-paid").textContent = `₹${checkoutData.amount}`;

        const successModal = new bootstrap.Modal(document.getElementById("bookingSuccessModal"));
        successModal.show();

        showToast("Slot booking confirmed successfully!", "success");

      } catch (err) {
        console.error(err);
        showToast("Network error. Please try again.", "danger");
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = `<i class="bi bi-check-circle-fill me-2"></i>Proceed to Pay & Confirm Slot`;
      }
    });

  } catch (err) {
    console.error(err);
    alert(err.message || "Error loading checkout details.");
  }
}

/* -------------------------------------------------------------
 * CUSTOMER BOOKING HISTORY (bookings.html)
 * ------------------------------------------------------------- */
async function initBookingHistory() {
  if (!requireAuth("CUSTOMER")) return;
  const user = getCurrentUser();
  if (!user || !user.id) {
    alert("Please log in to view your bookings.");
    window.location.href = "login.html";
    return;
  }
  await loadCustomerBookings(user.id);
}

async function loadCustomerBookings(userId) {
  const tbody = document.getElementById("bookings-tbody");
  tbody.innerHTML = `
    <tr>
      <td colspan="8" class="text-center py-4">
        <div class="spinner-border spinner-border-sm text-success" role="status"></div>
        <span class="ms-2 text-muted">Loading your booking history...</span>
      </td>
    </tr>
  `;

  try {
    const res = await fetch(`${API_BASE_URL}/bookings/user/${userId}`);
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Server returned ${res.status}: ${errText || 'Could not fetch bookings'}`);
    }
    const bookings = await res.json();

    if (!Array.isArray(bookings) || bookings.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="8" class="text-center py-4 text-muted">
            <i class="bi bi-calendar-x fs-2 d-block mb-2"></i>
            No bookings found. <a href="turfs.html" class="fw-bold text-success">Browse Turfs</a> to make your first booking!
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = bookings.map(b => {
      const isConfirmed = b.status === "CONFIRMED";
      const isPending = b.status === "PENDING";
      const isCancelled = b.status === "CANCELLED";
      const timeStr = b.slot ? `${formatTime(b.slot.startTime)} - ${formatTime(b.slot.endTime)}` : "N/A";
      const turfName = b.turf ? b.turf.name : "Turf Arena";
      const turfLoc = b.turf ? b.turf.location : "";

      return `
        <tr>
          <td class="fw-bold">#${b.id}</td>
          <td>
            <strong>${turfName}</strong><br>
            <small class="text-muted">${turfLoc}</small>
          </td>
          <td>${b.bookingDate || 'N/A'}</td>
          <td>${timeStr}</td>
          <td class="fw-bold">₹${b.totalAmount}</td>
          <td>
            <span class="badge badge-status-${b.status} px-2 py-1 rounded-pill">
              ${b.status}
            </span>
          </td>
          <td>
            ${isConfirmed ? '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>PAID</span>' : 
              isPending ? '<span class="badge bg-warning text-dark"><i class="bi bi-clock me-1"></i>UNPAID</span>' : 
              '<span class="badge bg-secondary">VOID</span>'}
          </td>
          <td>
            <div class="btn-group btn-group-sm">
              <button class="btn btn-outline-secondary" onclick="viewReceiptModal(${b.id})">
                <i class="bi bi-receipt me-1"></i>Receipt
              </button>
              ${!isCancelled ? `
                <button class="btn btn-outline-danger" onclick="cancelBookingAction(${b.id})">
                  <i class="bi bi-x-circle me-1"></i>Cancel
                </button>
              ` : `
                <span class="badge bg-light text-muted border p-2">Cancelled</span>
              `}
              ${isConfirmed && b.turf ? `
                <a href="reviews.html?turfId=${b.turf.id}&turfName=${encodeURIComponent(b.turf.name)}" class="btn btn-outline-primary" title="Review Turf">
                  <i class="bi bi-star"></i>
                </a>
              ` : ''}
            </div>
          </td>
        </tr>
      `;
    }).join("");

  } catch (err) {
    console.error("Error loading customer bookings:", err);
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center py-4 text-danger">
          <i class="bi bi-exclamation-triangle fs-3 d-block mb-2"></i>
          Failed to load bookings: ${err.message || 'Unknown error'}<br>
          <button class="btn btn-outline-success btn-sm mt-3" onclick="loadCustomerBookings(${userId})">
            <i class="bi bi-arrow-clockwise me-1"></i>Try Again
          </button>
        </td>
      </tr>
    `;
  }
}

// Cancel Booking
async function cancelBookingAction(bookingId) {
  if (!confirm(`Are you sure you want to cancel Booking #${bookingId}? This will release the slot.`)) {
    return;
  }

  try {
    const res = await fetch(`${API_BASE_URL}/bookings/${bookingId}/cancel`, {
      method: "PUT"
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast("Booking cancelled successfully.", "success");
      const user = getCurrentUser();
      if (user && user.id) {
        loadCustomerBookings(user.id);
      }
    } else {
      showToast(data.message || "Could not cancel booking", "danger");
    }
  } catch (err) {
    console.error(err);
    showToast("Network error cancelling booking", "danger");
  }
}

// View Receipt Modal
async function viewReceiptModal(bookingId) {
  try {
    const res = await fetch(`${API_BASE_URL}/bookings/${bookingId}`);
    if (!res.ok) throw new Error("Could not load booking details");
    const booking = await res.json();

    let paymentInfo = "Not Paid";
    try {
      const pRes = await fetch(`${API_BASE_URL}/payments/booking/${bookingId}`);
      if (pRes.ok) {
        const pData = await pRes.json();
        paymentInfo = `${pData.paymentMethod} (Txn: ${pData.transactionId})`;
      }
    } catch (e) {}

    const customerName = booking.user ? booking.user.name : "N/A";
    const customerPhone = booking.user ? (booking.user.phone || "N/A") : "N/A";
    const arenaName = booking.turf ? booking.turf.name : "N/A";
    const arenaLoc = booking.turf ? booking.turf.location : "N/A";
    const timeDisplay = booking.slot ? `${formatTime(booking.slot.startTime)} - ${formatTime(booking.slot.endTime)}` : "N/A";
    const createdAtTime = booking.createdAt ? new Date(booking.createdAt).getTime().toString().slice(-6) : "000000";

    const modalBody = document.getElementById("receipt-modal-body");
    modalBody.innerHTML = `
      <div class="p-3 border rounded bg-light mb-3">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="fw-bold text-success mb-0">SMART TURF BOOKING SYSTEM</h5>
          <span class="badge ${booking.status === 'CONFIRMED' ? 'bg-success' : 'bg-warning text-dark'}">${booking.status}</span>
        </div>
        <p class="small text-muted mb-0">Receipt ID: REC-${booking.id}-${createdAtTime}</p>
      </div>

      <table class="table table-sm table-bordered">
        <tbody>
          <tr><th class="bg-white">Customer Name</th><td>${customerName}</td></tr>
          <tr><th class="bg-white">Customer Phone</th><td>${customerPhone}</td></tr>
          <tr><th class="bg-white">Turf Arena</th><td>${arenaName}</td></tr>
          <tr><th class="bg-white">Location</th><td>${arenaLoc}</td></tr>
          <tr><th class="bg-white">Booking Date</th><td>${booking.bookingDate || 'N/A'}</td></tr>
          <tr><th class="bg-white">Time Slot</th><td>${timeDisplay}</td></tr>
          <tr><th class="bg-white">Payment Method</th><td>${paymentInfo}</td></tr>
          <tr class="table-active"><th class="fw-bold">Total Paid</th><td class="fw-bold text-success">₹${booking.totalAmount}</td></tr>
        </tbody>
      </table>
      <div class="text-center text-muted small mt-2">
        <i class="bi bi-shield-check me-1 text-success"></i>Verified system reservation. Please show this receipt at the venue.
      </div>
    `;

    const modal = new bootstrap.Modal(document.getElementById("receiptModal"));
    modal.show();

  } catch (err) {
    console.error(err);
    alert("Could not load receipt.");
  }
}
