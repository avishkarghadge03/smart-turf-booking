/**
 * Smart Turf Booking & Management System
 * Customer Complaints and Grievance JavaScript
 */

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth("CUSTOMER")) return;
  initComplaintsPage();
});

async function initComplaintsPage() {
  const user = getCurrentUser();
  await loadUserComplaints(user.id);

  const form = document.getElementById("complaint-form");
  if (form) {
    form.addEventListener("submit", submitComplaint);
  }
}

async function loadUserComplaints(userId) {
  const container = document.getElementById("complaints-list-container");
  if (!container) return;

  container.innerHTML = `
    <div class="text-center py-4 text-muted">
      <div class="spinner-border spinner-border-sm text-success" role="status"></div>
      <span class="ms-2">Loading complaints...</span>
    </div>
  `;

  try {
    const res = await fetch(`${API_BASE_URL}/complaints/user/${userId}`);
    if (!res.ok) throw new Error("Could not load complaints");
    const complaints = await res.json();

    if (complaints.length === 0) {
      container.innerHTML = `
        <div class="text-center py-4 text-muted border rounded bg-white">
          <i class="bi bi-shield-check fs-2 text-success"></i>
          <p class="mt-2 mb-0">No complaints registered. Everything is smooth!</p>
        </div>
      `;
      return;
    }

    container.innerHTML = complaints.map(c => `
      <div class="card border-0 shadow-sm mb-3">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h6 class="fw-bold text-dark mb-0">
              <span class="text-muted">#${c.id}</span> ${c.subject}
            </h6>
            <span class="badge badge-status-${c.status} px-2 py-1 rounded-pill">
              ${c.status}
            </span>
          </div>
          <p class="text-secondary small mb-2">${c.description}</p>
          <div class="d-flex justify-content-between align-items-center border-top pt-2 mt-2">
            <small class="text-muted" style="font-size: 0.75rem;">
              <i class="bi bi-clock me-1"></i>Reported: ${new Date(c.createdAt).toLocaleString()}
            </small>
            <span class="badge bg-light text-secondary border">Status: ${c.status}</span>
          </div>
        </div>
      </div>
    `).join("");

  } catch (err) {
    console.error(err);
    container.innerHTML = `<p class="text-danger small">Error loading complaints.</p>`;
  }
}

async function submitComplaint(e) {
  e.preventDefault();
  const user = getCurrentUser();
  const subject = document.getElementById("complaint-subject").value.trim();
  const description = document.getElementById("complaint-description").value.trim();

  if (!subject || !description) {
    alert("Please provide both subject and detailed description.");
    return;
  }

  const payload = {
    userId: user.id,
    subject: subject,
    description: description
  };

  try {
    const res = await fetch(`${API_BASE_URL}/complaints`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Complaint ticket logged successfully!", "success");
      document.getElementById("complaint-subject").value = "";
      document.getElementById("complaint-description").value = "";
      loadUserComplaints(user.id);
    } else {
      showToast(data.message || "Failed to submit complaint", "danger");
    }
  } catch (err) {
    console.error(err);
    showToast("Network error submitting complaint.", "danger");
  }
}
