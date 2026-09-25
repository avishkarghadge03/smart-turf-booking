/**
 * Smart Turf Booking & Management System
 * Home Page JavaScript (Featured turfs, Search)
 */

document.addEventListener("DOMContentLoaded", () => {
  loadFeaturedTurfs();

  const searchForm = document.getElementById("home-search-form");
  if (searchForm) {
    searchForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const query = document.getElementById("home-search-input").value.trim();
      if (query) {
        window.location.href = `turfs.html?search=${encodeURIComponent(query)}`;
      } else {
        window.location.href = "turfs.html";
      }
    });
  }
});

async function loadFeaturedTurfs() {
  const container = document.getElementById("featured-turfs-container");
  if (!container) return;

  try {
    const res = await fetch(`${API_BASE_URL}/turfs`);
    if (!res.ok) throw new Error("Failed to load turfs");
    const turfs = await res.json();

    if (turfs.length === 0) {
      container.innerHTML = `
        <div class="col-12 text-center py-4 text-muted">
          <p>No turfs available at the moment. Please check back later.</p>
        </div>
      `;
      return;
    }

    // Display first 3 featured turfs
    const featured = turfs.slice(0, 3);
    container.innerHTML = featured.map(turf => `
      <div class="col-md-4 mb-4">
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
              <small class="text-muted"><i class="bi bi-people me-1"></i>Up to ${turf.capacity} players</small>
            </div>
            <h5 class="card-title fw-bold text-dark">${turf.name}</h5>
            <p class="text-muted small mb-2"><i class="bi bi-geo-alt me-1 text-danger"></i>${turf.location}</p>
            <p class="card-text text-secondary small flex-grow-1">
              ${turf.description ? turf.description.substring(0, 95) + '...' : 'Premium sports arena with night lighting.'}
            </p>
            <div class="pt-3 border-top mt-auto d-flex gap-2">
              <a href="turf-details.html?id=${turf.id}" class="btn btn-outline-turf btn-sm flex-fill">View Details</a>
              <a href="turf-details.html?id=${turf.id}#book" class="btn btn-primary-turf btn-sm flex-fill">Book Now</a>
            </div>
          </div>
        </div>
      </div>
    `).join("");

  } catch (err) {
    console.error("Error loading featured turfs:", err);
    container.innerHTML = `
      <div class="col-12 text-center py-4 text-danger">
        <p>Could not connect to backend server. Please verify the Spring Boot application is running on port 8080.</p>
      </div>
    `;
  }
}
