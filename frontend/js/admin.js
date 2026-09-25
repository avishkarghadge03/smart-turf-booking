/**
 * Smart Turf Booking & Management System
 * Admin Panel JavaScript (Dashboard, Users, Turfs, Slots, Bookings, Payments, Reviews, Complaints)
 */

document.addEventListener("DOMContentLoaded", () => {
  // Guard admin pages
  if (!requireAuth("ADMIN")) return;

  // Initialize page-specific logic
  if (document.getElementById("admin-dashboard-view")) {
    initAdminDashboard();
  } else if (document.getElementById("admin-users-table")) {
    initAdminUsers();
  } else if (document.getElementById("admin-turfs-table")) {
    initAdminTurfs();
  } else if (document.getElementById("admin-turf-form")) {
    initAdminTurfForm();
  } else if (document.getElementById("admin-slots-view")) {
    initAdminSlots();
  } else if (document.getElementById("admin-bookings-table")) {
    initAdminBookings();
  } else if (document.getElementById("admin-payments-table")) {
    initAdminPayments();
  } else if (document.getElementById("admin-reviews-table")) {
    initAdminReviews();
  } else if (document.getElementById("admin-complaints-table")) {
    initAdminComplaints();
  }
});

/* -------------------------------------------------------------
 * 1. ADMIN DASHBOARD
 * ------------------------------------------------------------- */
async function initAdminDashboard() {
  try {
    const res = await fetch(`${API_BASE_URL}/admin/dashboard`);
    if (!res.ok) throw new Error("Could not load dashboard metrics");
    const stats = await res.json();

    document.getElementById("stat-users").textContent = stats.totalUsers || 0;
    document.getElementById("stat-turfs").textContent = stats.totalTurfs || 0;
    document.getElementById("stat-bookings").textContent = stats.totalBookings || 0;
    document.getElementById("stat-revenue").textContent = `₹${stats.totalRevenue || 0}`;
    document.getElementById("stat-complaints").textContent = stats.pendingComplaints || 0;

    // Load recent bookings table
    const bRes = await fetch(`${API_BASE_URL}/bookings`);
    if (bRes.ok) {
      const bookings = await bRes.json();
      const recent = bookings.slice(0, 5);
      const tbody = document.getElementById("admin-recent-bookings-tbody");
      if (recent.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-3 text-muted">No bookings recorded yet.</td></tr>`;
      } else {
        tbody.innerHTML = recent.map(b => `
          <tr>
            <td class="fw-bold">#${b.id}</td>
            <td>${b.user ? b.user.name : 'Unknown'}</td>
            <td>${b.turf ? b.turf.name : 'Unknown'}</td>
            <td>${b.bookingDate}</td>
            <td class="fw-bold">₹${b.totalAmount}</td>
            <td><span class="badge badge-status-${b.status}">${b.status}</span></td>
          </tr>
        `).join("");
      }
    }
  } catch (err) {
    console.error(err);
  }
}

/* -------------------------------------------------------------
 * 2. USER MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminUsers() {
  await loadUsersTable();

  const userForm = document.getElementById("admin-user-form");
  if (userForm) {
    userForm.addEventListener("submit", saveUserAction);
  }

  const roleFilter = document.getElementById("admin-user-role-filter");
  if (roleFilter) {
    roleFilter.addEventListener("change", loadUsersTable);
  }
}

async function loadUsersTable() {
  const tbody = document.getElementById("users-tbody");
  const roleFilter = document.getElementById("admin-user-role-filter") ? document.getElementById("admin-user-role-filter").value : "";

  try {
    const res = await fetch(`${API_BASE_URL}/users`);
    if (!res.ok) throw new Error("Could not load users");
    let users = await res.json();

    if (roleFilter) {
      users = users.filter(u => (u.role || "").toUpperCase() === roleFilter.toUpperCase());
    }

    if (users.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No users found for this filter.</td></tr>`;
      return;
    }

    tbody.innerHTML = users.map(u => {
      const roleBadge = u.role === 'ADMIN' ? 'bg-danger' :
                        u.role === 'OWNER' ? 'bg-warning text-dark' : 'bg-primary';
      return `
        <tr>
          <td class="fw-bold">#${u.id}</td>
          <td><strong>${u.name}</strong></td>
          <td>${u.email}</td>
          <td>${u.phone || '--'}</td>
          <td>
            <span class="badge ${roleBadge}">${u.role}</span>
          </td>
          <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A'}</td>
          <td>
            <div class="btn-group btn-group-sm">
              <button class="btn btn-outline-secondary" onclick='openEditUserModal(${JSON.stringify(u)})' title="Edit User">
                <i class="bi bi-pencil"></i>
              </button>
              <button class="btn btn-outline-danger" onclick="deleteUserAction(${u.id})" title="Delete User">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join("");
  } catch (err) {
    console.error(err);
  }
}

function openAddUserModal() {
  document.getElementById("user-modal-title").textContent = "Add New User";
  document.getElementById("user-id").value = "";
  document.getElementById("user-name").value = "";
  document.getElementById("user-email").value = "";
  document.getElementById("user-password").value = "";
  document.getElementById("user-phone").value = "";
  document.getElementById("user-role").value = "CUSTOMER";
  const modal = new bootstrap.Modal(document.getElementById("userModal"));
  modal.show();
}

function openEditUserModal(user) {
  document.getElementById("user-modal-title").textContent = "Edit User";
  document.getElementById("user-id").value = user.id;
  document.getElementById("user-name").value = user.name;
  document.getElementById("user-email").value = user.email;
  document.getElementById("user-password").value = ""; // Leave blank to keep existing
  document.getElementById("user-phone").value = user.phone;
  document.getElementById("user-role").value = user.role;
  const modal = new bootstrap.Modal(document.getElementById("userModal"));
  modal.show();
}

async function saveUserAction(e) {
  e.preventDefault();
  const id = document.getElementById("user-id").value;
  const isEdit = id && id.trim() !== "";

  const payload = {
    name: document.getElementById("user-name").value.trim(),
    email: document.getElementById("user-email").value.trim(),
    password: document.getElementById("user-password").value,
    phone: document.getElementById("user-phone").value.trim(),
    role: document.getElementById("user-role").value
  };

  if (!isEdit && !payload.password) {
    alert("Password is required for new users.");
    return;
  }

  try {
    let url = `${API_BASE_URL}/users`;
    let method = "POST";
    if (isEdit) {
      url += `/${id}`;
      method = "PUT";
    }

    const res = await fetch(url, {
      method: method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast(isEdit ? "User updated successfully!" : "User created successfully!", "success");
      const modal = bootstrap.Modal.getInstance(document.getElementById("userModal"));
      if (modal) modal.hide();
      loadUsersTable();
    } else {
      showToast(data.message || "Operation failed", "danger");
    }
  } catch (err) {
    console.error(err);
    showToast("Network error saving user.", "danger");
  }
}

async function deleteUserAction(id) {
  const current = getCurrentUser();
  if (current && current.id === id) {
    alert("You cannot delete your own active administrator account.");
    return;
  }
  if (!confirm(`Are you sure you want to delete User #${id}?`)) return;

  try {
    const res = await fetch(`${API_BASE_URL}/users/${id}`, { method: "DELETE" });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast("User deleted successfully.", "success");
      loadUsersTable();
    } else {
      showToast(data.message || "Failed to delete user", "danger");
    }
  } catch (err) {
    console.error(err);
    showToast("Network error deleting user.", "danger");
  }
}

/* -------------------------------------------------------------
 * 3. TURF MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminTurfs() {
  await loadAdminTurfsTable();
}

async function loadAdminTurfsTable() {
  const tbody = document.getElementById("turfs-tbody");
  try {
    const res = await fetch(`${API_BASE_URL}/turfs`);
    if (!res.ok) throw new Error("Could not load turfs");
    const turfs = await res.json();

    tbody.innerHTML = turfs.map(t => `
      <tr>
        <td class="fw-bold">#${t.id}</td>
        <td>
          <img src="${t.imageUrl || 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=100&q=80'}" 
               style="width: 50px; height: 35px; object-fit: cover; border-radius: 4px;" class="me-2">
          <strong>${t.name}</strong>
        </td>
        <td>${t.location}</td>
        <td>${t.sportType}</td>
        <td>${t.capacity} Players</td>
        <td class="fw-bold">₹${t.pricePerHour}/hr</td>
        <td>
          <span class="badge ${t.status === 'AVAILABLE' ? 'bg-success' : 'bg-danger'}">${t.status}</span>
        </td>
        <td>
          <div class="btn-group btn-group-sm">
            <a href="turf-form.html?id=${t.id}" class="btn btn-outline-secondary" title="Edit Turf">
              <i class="bi bi-pencil"></i>
            </a>
            <a href="slots.html?turfId=${t.id}" class="btn btn-outline-primary" title="Manage Slots">
              <i class="bi bi-clock"></i>
            </a>
            <button class="btn btn-outline-danger" onclick="deleteTurfAction(${t.id})" title="Delete/Disable Turf">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error(err);
  }
}

async function deleteTurfAction(id) {
  if (!confirm(`Are you sure you want to delete/deactivate Turf #${id}? If bookings exist, it will be marked UNAVAILABLE.`)) return;

  try {
    const res = await fetch(`${API_BASE_URL}/turfs/${id}`, { method: "DELETE" });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Turf deleted/deactivated successfully.", "success");
      loadAdminTurfsTable();
    } else {
      showToast(data.message || "Failed to delete turf", "danger");
    }
  } catch (err) {
    console.error(err);
  }
}

/* -------------------------------------------------------------
 * 3B. TURF FORM (Add / Edit)
 * ------------------------------------------------------------- */
async function initAdminTurfForm() {
  const params = new URLSearchParams(window.location.search);
  const turfId = params.get("id");
  const form = document.getElementById("admin-turf-form");
  const formTitle = document.getElementById("turf-form-title");

  if (turfId) {
    formTitle.textContent = "Edit Turf Arena";
    document.getElementById("turf-id").value = turfId;
    try {
      const res = await fetch(`${API_BASE_URL}/turfs/${turfId}`);
      if (!res.ok) throw new Error("Could not find turf");
      const t = await res.json();

      document.getElementById("turf-name").value = t.name;
      document.getElementById("turf-location").value = t.location;
      document.getElementById("turf-sport").value = t.sportType;
      document.getElementById("turf-price").value = t.pricePerHour;
      document.getElementById("turf-capacity").value = t.capacity;
      document.getElementById("turf-image").value = t.imageUrl || "";
      document.getElementById("turf-status").value = t.status;
      document.getElementById("turf-description").value = t.description || "";
    } catch (e) {
      alert("Error loading turf.");
    }
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("turf-id").value;
    const isEdit = id && id.trim() !== "";

    const payload = {
      name: document.getElementById("turf-name").value.trim(),
      location: document.getElementById("turf-location").value.trim(),
      sportType: document.getElementById("turf-sport").value.trim(),
      pricePerHour: parseFloat(document.getElementById("turf-price").value),
      capacity: parseInt(document.getElementById("turf-capacity").value, 10),
      imageUrl: document.getElementById("turf-image").value.trim(),
      status: document.getElementById("turf-status").value,
      description: document.getElementById("turf-description").value.trim()
    };

    try {
      let url = `${API_BASE_URL}/turfs`;
      let method = "POST";
      if (isEdit) {
        url += `/${id}`;
        method = "PUT";
      }

      const res = await fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      const data = await res.json();
      if (res.ok && data.success) {
        alert(isEdit ? "Turf updated successfully!" : "Turf created successfully!");
        window.location.href = "turfs.html";
      } else {
        alert(data.message || "Failed to save turf.");
      }
    } catch (err) {
      console.error(err);
      alert("Network error saving turf.");
    }
  });
}

/* -------------------------------------------------------------
 * 4. SLOT MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminSlots() {
  const turfSelect = document.getElementById("filter-turf-select");
  const batchTurfSelect = document.getElementById("batch-turf-select");
  const singleTurfSelect = document.getElementById("slot-turf-select");
  const datePicker = document.getElementById("filter-slot-date");

  const todayStr = new Date().toISOString().split("T")[0];
  datePicker.value = todayStr;

  // Populate turf dropdowns
  try {
    const res = await fetch(`${API_BASE_URL}/turfs`);
    if (res.ok) {
      const turfs = await res.json();
      const options = turfs.map(t => `<option value="${t.id}">${t.name}</option>`).join("");
      turfSelect.innerHTML = `<option value="">-- All Turfs --</option>` + options;
      if (batchTurfSelect) batchTurfSelect.innerHTML = options;
      if (singleTurfSelect) singleTurfSelect.innerHTML = options;

      // Check URL param
      const params = new URLSearchParams(window.location.search);
      const paramTurfId = params.get("turfId");
      if (paramTurfId) {
        turfSelect.value = paramTurfId;
      }
    }
  } catch (e) {}

  await loadSlotsTable();

  turfSelect.addEventListener("change", loadSlotsTable);
  datePicker.addEventListener("change", loadSlotsTable);

  // Single Slot Form
  const singleForm = document.getElementById("single-slot-form");
  if (singleForm) {
    singleForm.addEventListener("submit", saveSingleSlot);
  }

  // Batch Generation Form
  const batchForm = document.getElementById("batch-slot-form");
  if (batchForm) {
    batchForm.addEventListener("submit", generateBatchSlots);
  }
}

async function loadSlotsTable() {
  const tbody = document.getElementById("slots-tbody");
  const turfId = document.getElementById("filter-turf-select").value;
  const date = document.getElementById("filter-slot-date").value;

  tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">Loading slots...</td></tr>`;

  try {
    let url = `${API_BASE_URL}/slots`;
    if (turfId && date) {
      url = `${API_BASE_URL}/slots/turf/${turfId}?date=${date}`;
    } else if (turfId) {
      url = `${API_BASE_URL}/slots/turf/${turfId}`;
    }

    const res = await fetch(url);
    if (!res.ok) throw new Error("Could not load slots");
    let slots = await res.json();

    if (date && !turfId) {
      slots = slots.filter(s => s.slotDate === date);
    }

    if (slots.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No slots found matching criteria.</td></tr>`;
      return;
    }

    tbody.innerHTML = slots.map(s => `
      <tr>
        <td class="fw-bold">#${s.id}</td>
        <td><strong>${s.turf ? s.turf.name : 'Unknown'}</strong></td>
        <td>${s.slotDate}</td>
        <td>${formatTime(s.startTime)} - ${formatTime(s.endTime)}</td>
        <td>
          <span class="badge ${s.status === 'AVAILABLE' ? 'bg-success' : 'bg-danger'}">
            ${s.status}
          </span>
        </td>
        <td>
          <button class="btn btn-sm ${s.status === 'AVAILABLE' ? 'btn-outline-warning' : 'btn-outline-success'}"
                  onclick="toggleSlotStatus(${s.id}, '${s.status === 'AVAILABLE' ? 'BOOKED' : 'AVAILABLE'}')">
            Mark ${s.status === 'AVAILABLE' ? 'Booked' : 'Available'}
          </button>
        </td>
        <td>
          <button class="btn btn-sm btn-outline-danger" onclick="deleteSlotAction(${s.id})">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error(err);
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-danger">Error loading slots.</td></tr>`;
  }
}

async function saveSingleSlot(e) {
  e.preventDefault();
  const payload = {
    turfId: parseInt(document.getElementById("slot-turf-select").value, 10),
    slotDate: document.getElementById("slot-date").value,
    startTime: document.getElementById("slot-start-time").value + ":00",
    endTime: document.getElementById("slot-end-time").value + ":00",
    status: document.getElementById("slot-status").value
  };

  try {
    const res = await fetch(`${API_BASE_URL}/slots`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Slot added successfully!", "success");
      const modal = bootstrap.Modal.getInstance(document.getElementById("singleSlotModal"));
      if (modal) modal.hide();
      loadSlotsTable();
    } else {
      showToast(data.message || "Failed to create slot", "danger");
    }
  } catch (err) {
    showToast("Network error creating slot.", "danger");
  }
}

async function generateBatchSlots(e) {
  e.preventDefault();
  const turfId = document.getElementById("batch-turf-select").value;
  const date = document.getElementById("batch-date").value;
  const startHour = document.getElementById("batch-start-hour").value;
  const endHour = document.getElementById("batch-end-hour").value;

  try {
    const res = await fetch(`${API_BASE_URL}/slots/generate?turfId=${turfId}&date=${date}&startHour=${startHour}&endHour=${endHour}`, {
      method: "POST"
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast(data.message, "success");
      const modal = bootstrap.Modal.getInstance(document.getElementById("batchSlotModal"));
      if (modal) modal.hide();
      loadSlotsTable();
    } else {
      showToast(data.message || "Error generating slots", "danger");
    }
  } catch (err) {
    showToast("Network error generating batch slots", "danger");
  }
}

async function toggleSlotStatus(slotId, newStatus) {
  try {
    const res = await fetch(`${API_BASE_URL}/slots/${slotId}`);
    if (!res.ok) throw new Error("Could not find slot");
    const slot = await res.json();

    const payload = {
      turfId: slot.turf.id,
      slotDate: slot.slotDate,
      startTime: slot.startTime,
      endTime: slot.endTime,
      status: newStatus
    };

    const updateRes = await fetch(`${API_BASE_URL}/slots/${slotId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (updateRes.ok) {
      showToast(`Slot marked as ${newStatus}`, "success");
      loadSlotsTable();
    }
  } catch (err) {
    console.error(err);
  }
}

async function deleteSlotAction(id) {
  if (!confirm(`Delete Slot #${id}?`)) return;
  try {
    const res = await fetch(`${API_BASE_URL}/slots/${id}`, { method: "DELETE" });
    if (res.ok) {
      showToast("Slot deleted.", "success");
      loadSlotsTable();
    }
  } catch (err) {
    console.error(err);
  }
}

/* -------------------------------------------------------------
 * 5. BOOKINGS MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminBookings() {
  await loadAdminBookingsTable();

  const filterSelect = document.getElementById("booking-status-filter");
  if (filterSelect) {
    filterSelect.addEventListener("change", loadAdminBookingsTable);
  }
}

async function loadAdminBookingsTable() {
  const tbody = document.getElementById("admin-bookings-tbody");
  const filter = document.getElementById("booking-status-filter") ? document.getElementById("booking-status-filter").value : "";

  try {
    const res = await fetch(`${API_BASE_URL}/bookings`);
    if (!res.ok) throw new Error("Could not fetch bookings");
    let bookings = await res.json();

    if (filter) {
      bookings = bookings.filter(b => b.status === filter);
    }

    if (bookings.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-muted">No bookings found.</td></tr>`;
      return;
    }

    tbody.innerHTML = bookings.map(b => `
      <tr>
        <td class="fw-bold">#${b.id}</td>
        <td>
          <strong>${b.user ? b.user.name : 'Unknown'}</strong><br>
          <small class="text-muted">${b.user ? b.user.phone : ''}</small>
        </td>
        <td>${b.turf ? b.turf.name : 'Unknown'}</td>
        <td>${b.bookingDate}</td>
        <td>${b.slot ? `${formatTime(b.slot.startTime)} - ${formatTime(b.slot.endTime)}` : 'N/A'}</td>
        <td class="fw-bold">₹${b.totalAmount}</td>
        <td><span class="badge badge-status-${b.status}">${b.status}</span></td>
        <td>
          <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
              Update Status
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li><a class="dropdown-item" href="javascript:void(0)" onclick="updateAdminBookingStatus(${b.id}, 'CONFIRMED')">Set CONFIRMED</a></li>
              <li><a class="dropdown-item" href="javascript:void(0)" onclick="updateAdminBookingStatus(${b.id}, 'PENDING')">Set PENDING</a></li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="updateAdminBookingStatus(${b.id}, 'CANCELLED')">Cancel & Release Slot</a></li>
            </ul>
          </div>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error(err);
  }
}

async function updateAdminBookingStatus(id, newStatus) {
  try {
    let url = `${API_BASE_URL}/bookings/${id}`;
    let method = "PUT";
    let body = JSON.stringify({ status: newStatus });

    if (newStatus === "CANCELLED") {
      url = `${API_BASE_URL}/bookings/${id}/cancel`;
      body = null;
    }

    const res = await fetch(url, {
      method: method,
      headers: body ? { "Content-Type": "application/json" } : {},
      body: body
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast(`Booking #${id} updated to ${newStatus}`, "success");
      loadAdminBookingsTable();
    } else {
      showToast(data.message || "Could not update status", "danger");
    }
  } catch (err) {
    console.error(err);
  }
}

/* -------------------------------------------------------------
 * 6. PAYMENTS MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminPayments() {
  const tbody = document.getElementById("admin-payments-tbody");
  try {
    const res = await fetch(`${API_BASE_URL}/payments`);
    if (!res.ok) throw new Error("Could not load payments");
    const payments = await res.json();

    if (payments.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No payment records found.</td></tr>`;
      return;
    }

    tbody.innerHTML = payments.map(p => `
      <tr>
        <td class="fw-bold">#${p.id}</td>
        <td><code>${p.transactionId}</code></td>
        <td>Booking #${p.booking ? p.booking.id : 'N/A'}</td>
        <td class="fw-bold text-success">₹${p.amount}</td>
        <td><span class="badge bg-light text-dark border">${p.paymentMethod}</span></td>
        <td><span class="badge badge-status-${p.paymentStatus}">${p.paymentStatus}</span></td>
        <td>${new Date(p.paymentDate).toLocaleString()}</td>
      </tr>
    `).join("");
  } catch (err) {
    console.error(err);
  }
}

/* -------------------------------------------------------------
 * 7. REVIEWS MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminReviews() {
  await loadAdminReviewsTable();
}

async function loadAdminReviewsTable() {
  const tbody = document.getElementById("admin-reviews-tbody");
  try {
    const res = await fetch(`${API_BASE_URL}/reviews`);
    if (!res.ok) throw new Error("Could not load reviews");
    const reviews = await res.json();

    if (reviews.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No customer reviews yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = reviews.map(r => `
      <tr>
        <td class="fw-bold">#${r.id}</td>
        <td>${r.turf ? r.turf.name : 'Unknown'}</td>
        <td>${r.user ? r.user.name : 'Unknown'}</td>
        <td><div class="star-rating">${"★".repeat(r.rating)}${"☆".repeat(5 - r.rating)}</div></td>
        <td>${r.comment}</td>
        <td>${new Date(r.createdAt).toLocaleDateString()}</td>
        <td>
          <button class="btn btn-sm btn-outline-danger" onclick="deleteAdminReviewAction(${r.id})">
            <i class="bi bi-trash me-1"></i>Delete
          </button>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error(err);
  }
}

async function deleteAdminReviewAction(id) {
  if (!confirm(`Delete Review #${id}?`)) return;
  try {
    const res = await fetch(`${API_BASE_URL}/reviews/${id}`, { method: "DELETE" });
    if (res.ok) {
      showToast("Review deleted successfully.", "success");
      loadAdminReviewsTable();
    }
  } catch (err) {
    console.error(err);
  }
}

/* -------------------------------------------------------------
 * 8. COMPLAINTS MANAGEMENT
 * ------------------------------------------------------------- */
async function initAdminComplaints() {
  await loadAdminComplaintsTable();

  const filterSelect = document.getElementById("complaint-status-filter");
  if (filterSelect) {
    filterSelect.addEventListener("change", loadAdminComplaintsTable);
  }
}

async function loadAdminComplaintsTable() {
  const tbody = document.getElementById("admin-complaints-tbody");
  const filter = document.getElementById("complaint-status-filter") ? document.getElementById("complaint-status-filter").value : "";

  try {
    const res = await fetch(`${API_BASE_URL}/complaints`);
    if (!res.ok) throw new Error("Could not load complaints");
    let complaints = await res.json();

    if (filter) {
      complaints = complaints.filter(c => c.status === filter);
    }

    if (complaints.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No complaints match filter.</td></tr>`;
      return;
    }

    tbody.innerHTML = complaints.map(c => `
      <tr>
        <td class="fw-bold">#${c.id}</td>
        <td>
          <strong>${c.user ? c.user.name : 'Unknown'}</strong><br>
          <small class="text-muted">${c.user ? c.user.email : ''}</small>
        </td>
        <td class="fw-semibold">${c.subject}</td>
        <td><small class="text-secondary">${c.description}</small></td>
        <td><span class="badge badge-status-${c.status}">${c.status}</span></td>
        <td>${new Date(c.createdAt).toLocaleDateString()}</td>
        <td>
          <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
              Action
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li><a class="dropdown-item text-warning" href="javascript:void(0)" onclick="updateComplaintStatusAction(${c.id}, 'IN_PROGRESS')">Mark IN_PROGRESS</a></li>
              <li><a class="dropdown-item text-success" href="javascript:void(0)" onclick="updateComplaintStatusAction(${c.id}, 'RESOLVED')">Mark RESOLVED</a></li>
              <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="updateComplaintStatusAction(${c.id}, 'OPEN')">Reopen ticket (OPEN)</a></li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="deleteComplaintAction(${c.id})"><i class="bi bi-trash me-1"></i>Delete</a></li>
            </ul>
          </div>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error(err);
  }
}

async function updateComplaintStatusAction(id, status) {
  try {
    const res = await fetch(`${API_BASE_URL}/complaints/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: status })
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast(`Complaint #${id} marked as ${status}`, "success");
      loadAdminComplaintsTable();
    }
  } catch (err) {
    console.error(err);
  }
}

async function deleteComplaintAction(id) {
  if (!confirm(`Delete Complaint #${id}?`)) return;
  try {
    const res = await fetch(`${API_BASE_URL}/complaints/${id}`, { method: "DELETE" });
    if (res.ok) {
      showToast("Complaint deleted.", "success");
      loadAdminComplaintsTable();
    }
  } catch (err) {
    console.error(err);
  }
}

function formatTime(timeStr) {
  if (!timeStr) return "";
  const parts = timeStr.split(":");
  let hour = parseInt(parts[0], 10);
  const minute = parts[1];
  const ampm = hour >= 12 ? "PM" : "AM";
  hour = hour % 12;
  hour = hour ? hour : 12;
  return `${hour}:${minute} ${ampm}`;
}
