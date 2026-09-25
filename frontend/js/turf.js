/**
 * Smart Turf Booking & Management System
 * Turf Browsing, Details, and Slot Selector JavaScript
 */

let allTurfsList = [];
let selectedSlotId = null;
let currentTurfData = null;

// Initialize depending on current page
document.addEventListener("DOMContentLoaded", () => {
  if (document.getElementById("turfs-list-container")) {
    initTurfListPage();
  } else if (document.getElementById("turf-detail-container")) {
    initTurfDetailPage();
  }
});

/* -------------------------------------------------------------
 * TURF LIST PAGE (turfs.html)
 * ------------------------------------------------------------- */
async function initTurfListPage() {
  const urlParams = new URLSearchParams(window.location.search);
  const searchParam = urlParams.get("search");

  if (searchParam) {
    const searchInput = document.getElementById("search-input");
    if (searchInput) searchInput.value = searchParam;
  }

  await loadTurfsList(searchParam);

  // Search input listener
  const searchForm = document.getElementById("turf-search-form");
  if (searchForm) {
    searchForm.addEventListener("submit", (e) => {
      e.preventDefault();
      applyFilters();
    });
  }

  // Filter dropdown listeners
  const sportFilter = document.getElementById("sport-filter");
  const priceFilter = document.getElementById("price-filter");
  if (sportFilter) sportFilter.addEventListener("change", applyFilters);
  if (priceFilter) priceFilter.addEventListener("change", applyFilters);
}

async function loadTurfsList(query = null) {
  const container = document.getElementById("turfs-list-container");
  container.innerHTML = `
    <div class="col-12 text-center py-5">
      <div class="spinner-border text-success" role="status"></div>
      <p class="mt-2 text-muted">Loading turfs...</p>
    </div>
  `;

  try {
    let url = `${API_BASE_URL}/turfs`;
    if (query) {
      url += `?search=${encodeURIComponent(query)}`;
    }
    const res = await fetch(url);
    if (!res.ok) throw new Error("Could not fetch turfs");
    allTurfsList = await res.json();
    renderTurfs(allTurfsList);
  } catch (err) {
    console.error(err);
    container.innerHTML = `
      <div class="col-12 text-center py-5 text-danger">
        <i class="bi bi-exclamation-triangle fs-1"></i>
        <h5 class="mt-2">Failed to load turfs</h5>
        <p class="text-muted">Ensure the Spring Boot backend is active on http://localhost:8080</p>
      </div>
    `;
  }
}

function renderTurfs(turfs) {
  const container = document.getElementById("turfs-list-container");
  const countSpan = document.getElementById("turf-count");
  if (countSpan) countSpan.textContent = `(${turfs.length} found)`;

  if (turfs.length === 0) {
    container.innerHTML = `
      <div class="col-12 text-center py-5 text-muted">
        <i class="bi bi-search fs-1"></i>
        <h5 class="mt-3">No turfs match your criteria</h5>
        <p>Try searching for a different keyword or resetting filters.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = turfs.map(turf => `
    <div class="col-md-6 col-lg-4 mb-4">
      <div class="card turf-card h-100 position-relative">
        <div class="position-relative">
          <img src="${turf.imageUrl || 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=600&q=80'}" 
               class="turf-card-img" alt="${turf.name}"
               onerror="this.src='https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=600&q=80'">
          <span class="turf-price-badge">₹${turf.pricePerHour}/hr</span>
          <span class="badge ${turf.status === 'AVAILABLE' ? 'bg-success' : 'bg-danger'} turf-status-badge">
            ${turf.status}
          </span>
        </div>
        <div class="card-body d-flex flex-column">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <span class="badge bg-light text-dark border">${turf.sportType}</span>
            <small class="text-muted"><i class="bi bi-people me-1"></i>Max ${turf.capacity}</small>
          </div>
          <h5 class="card-title fw-bold text-dark">${turf.name}</h5>
          <p class="text-muted small mb-2"><i class="bi bi-geo-alt me-1 text-danger"></i>${turf.location}</p>
          <p class="card-text text-secondary small flex-grow-1">
            ${turf.description ? turf.description.substring(0, 95) + '...' : 'Clean arena with modern artificial grass turf.'}
          </p>
          <div class="pt-3 border-top mt-auto d-flex gap-2">
            <a href="turf-details.html?id=${turf.id}" class="btn btn-outline-turf btn-sm flex-fill">View Details</a>
            <a href="turf-details.html?id=${turf.id}#book" class="btn btn-primary-turf btn-sm flex-fill">Book Now</a>
          </div>
        </div>
      </div>
    </div>
  `).join("");
}

function applyFilters() {
  const query = document.getElementById("search-input") ? document.getElementById("search-input").value.toLowerCase().trim() : "";
  const sport = document.getElementById("sport-filter") ? document.getElementById("sport-filter").value : "";
  const priceSort = document.getElementById("price-filter") ? document.getElementById("price-filter").value : "";

  let filtered = allTurfsList.filter(t => {
    const matchesQuery = !query || 
      t.name.toLowerCase().includes(query) || 
      t.location.toLowerCase().includes(query) ||
      t.sportType.toLowerCase().includes(query);

    const matchesSport = !sport || t.sportType.toLowerCase().includes(sport.toLowerCase());

    return matchesQuery && matchesSport;
  });

  if (priceSort === "low") {
    filtered.sort((a, b) => a.pricePerHour - b.pricePerHour);
  } else if (priceSort === "high") {
    filtered.sort((a, b) => b.pricePerHour - a.pricePerHour);
  }

  renderTurfs(filtered);
}

/* -------------------------------------------------------------
 * TURF DETAILS & SLOT PICKER (turf-details.html)
 * ------------------------------------------------------------- */
async function initTurfDetailPage() {
  const urlParams = new URLSearchParams(window.location.search);
  const turfId = urlParams.get("id");

  if (!turfId) {
    alert("Invalid Turf ID");
    window.location.href = "turfs.html";
    return;
  }

  await loadTurfDetails(turfId);
  await loadTurfReviews(turfId);

  // Set today's date as default in date input and min date
  const dateInput = document.getElementById("slot-date-picker");
  if (dateInput) {
    const todayStr = new Date().toISOString().split("T")[0];
    dateInput.value = todayStr;
    dateInput.min = todayStr;

    dateInput.addEventListener("change", () => {
      loadSlotsForDate(turfId, dateInput.value);
    });

    // Initial slot load
    loadSlotsForDate(turfId, todayStr);
  }

  // Handle Book Now Click
  const proceedBtn = document.getElementById("proceed-booking-btn");
  if (proceedBtn) {
    proceedBtn.addEventListener("click", () => {
      if (!isLoggedIn()) {
        alert("Please log in to proceed with booking.");
        window.location.href = `login.html?redirect=turf-details.html?id=${turfId}`;
        return;
      }
      if (!selectedSlotId) {
        alert("Please select an available time slot before continuing.");
        return;
      }
      const selectedDate = document.getElementById("slot-date-picker").value;
      window.location.href = `booking.html?turfId=${turfId}&slotId=${selectedSlotId}&date=${selectedDate}`;
    });
  }
}

async function loadTurfDetails(turfId) {
  try {
    const res = await fetch(`${API_BASE_URL}/turfs/${turfId}`);
    if (!res.ok) throw new Error("Turf not found");
    currentTurfData = await res.json();

    document.getElementById("detail-turf-name").textContent = currentTurfData.name;
    document.getElementById("detail-turf-location").textContent = currentTurfData.location;
    document.getElementById("detail-turf-price").textContent = `₹${currentTurfData.pricePerHour}/hr`;
    document.getElementById("detail-turf-sport").textContent = currentTurfData.sportType;
    document.getElementById("detail-turf-capacity").textContent = `${currentTurfData.capacity} Players`;
    document.getElementById("detail-turf-description").textContent = currentTurfData.description || "No description provided.";
    
    const imgEl = document.getElementById("detail-turf-img");
    if (imgEl) {
      imgEl.src = currentTurfData.imageUrl || 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=1000&q=80';
    }

    const statusBadge = document.getElementById("detail-turf-status");
    if (statusBadge) {
      statusBadge.textContent = currentTurfData.status;
      statusBadge.className = `badge ${currentTurfData.status === 'AVAILABLE' ? 'bg-success' : 'bg-danger'}`;
    }

    // Fetch average rating
    const ratingRes = await fetch(`${API_BASE_URL}/reviews/turf/${turfId}/average`);
    if (ratingRes.ok) {
      const rData = await ratingRes.json();
      const avgSpan = document.getElementById("detail-avg-rating");
      if (avgSpan) {
        avgSpan.textContent = rData.averageRating > 0 ? `★ ${rData.averageRating}` : "★ New";
      }
    }

  } catch (err) {
    console.error(err);
    alert("Could not load turf details.");
  }
}

async function loadSlotsForDate(turfId, date) {
  const container = document.getElementById("slots-container");
  selectedSlotId = null;
  const proceedBtn = document.getElementById("proceed-booking-btn");
  if (proceedBtn) proceedBtn.disabled = true;

  container.innerHTML = `
    <div class="text-center py-4 text-muted">
      <div class="spinner-border spinner-border-sm text-success" role="status"></div>
      <p class="mt-2 mb-0 small">Checking slot availability...</p>
    </div>
  `;

  try {
    const res = await fetch(`${API_BASE_URL}/slots/turf/${turfId}?date=${date}`);
    if (!res.ok) throw new Error("Could not fetch slots");
    const slots = await res.json();

    if (slots.length === 0) {
      container.innerHTML = `
        <div class="text-center py-4 text-muted">
          <i class="bi bi-clock-history fs-3 text-secondary"></i>
          <p class="mt-2 mb-1 fw-semibold">No slots scheduled for this date.</p>
          <small>Please pick another date or contact turf admin.</small>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div class="slot-grid mt-2">
        ${slots.map(slot => {
          const isBooked = slot.status === "BOOKED";
          const timeText = `${formatTime(slot.startTime)} - ${formatTime(slot.endTime)}`;
          return `
            <button type="button" 
                    class="slot-btn ${isBooked ? 'booked' : ''}" 
                    data-slot-id="${slot.id}"
                    ${isBooked ? 'disabled title="Already Booked"' : `onclick="selectSlot(${slot.id}, this)"`}>
              <div>${timeText}</div>
              <small class="d-block mt-1 ${isBooked ? 'text-danger' : 'text-success'} fw-bold" style="font-size: 0.72rem;">
                ${slot.status}
              </small>
            </button>
          `;
        }).join("")}
      </div>
    `;

  } catch (err) {
    console.error(err);
    container.innerHTML = `<p class="text-danger small">Error loading slots.</p>`;
  }
}

function selectSlot(slotId, buttonEl) {
  selectedSlotId = slotId;

  // Highlight selected button
  document.querySelectorAll(".slot-btn").forEach(btn => btn.classList.remove("selected"));
  buttonEl.classList.add("selected");

  // Enable Proceed button
  const proceedBtn = document.getElementById("proceed-booking-btn");
  if (proceedBtn) {
    proceedBtn.disabled = false;
  }
}

function formatTime(timeStr) {
  if (!timeStr) return "";
  const parts = timeStr.split(":");
  let hour = parseInt(parts[0], 10);
  const minute = parts[1];
  const ampm = hour >= 12 ? "PM" : "AM";
  hour = hour % 12;
  hour = hour ? hour : 12; // 0 hour should be 12
  return `${hour}:${minute} ${ampm}`;
}

async function loadTurfReviews(turfId) {
  const container = document.getElementById("turf-reviews-list");
  if (!container) return;

  try {
    const res = await fetch(`${API_BASE_URL}/reviews/turf/${turfId}`);
    if (!res.ok) return;
    const reviews = await res.json();

    if (reviews.length === 0) {
      container.innerHTML = `<p class="text-muted small">No reviews submitted yet. Be the first to play and review!</p>`;
      return;
    }

    container.innerHTML = reviews.map(r => `
      <div class="border-bottom pb-3 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-1">
          <strong class="text-dark"><i class="bi bi-person-circle me-1 text-secondary"></i>${r.user.name}</strong>
          <div class="star-rating">${"★".repeat(r.rating)}${"☆".repeat(5 - r.rating)}</div>
        </div>
        <p class="mb-1 text-secondary small">${r.comment}</p>
        <small class="text-muted" style="font-size: 0.75rem;">${new Date(r.createdAt).toLocaleDateString()}</small>
      </div>
    `).join("");

  } catch (err) {
    console.warn("Could not load reviews:", err);
  }
}
