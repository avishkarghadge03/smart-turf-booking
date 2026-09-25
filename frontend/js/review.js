/**
 * Smart Turf Booking & Management System
 * Reviews and Ratings JavaScript
 */

let selectedRatingValue = 5;

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth("CUSTOMER")) return;
  initReviewsPage();
});

async function initReviewsPage() {
  const user = getCurrentUser();
  setupStarPicker();
  await loadCustomerTurfsForReview(user.id);
  await loadCustomerReviewsList(user.id);

  const reviewForm = document.getElementById("review-form");
  if (reviewForm) {
    reviewForm.addEventListener("submit", submitReview);
  }
}

function setupStarPicker() {
  const stars = document.querySelectorAll(".star-picker i");
  stars.forEach(star => {
    star.addEventListener("click", () => {
      const val = parseInt(star.getAttribute("data-rating"), 10);
      selectedRatingValue = val;
      updateStarDisplay(val);
    });
  });
  updateStarDisplay(5);
}

function updateStarDisplay(rating) {
  const stars = document.querySelectorAll(".star-picker i");
  stars.forEach(s => {
    const val = parseInt(s.getAttribute("data-rating"), 10);
    if (val <= rating) {
      s.classList.add("active");
      s.classList.remove("bi-star");
      s.classList.add("bi-star-fill");
    } else {
      s.classList.remove("active");
      s.classList.remove("bi-star-fill");
      s.classList.add("bi-star");
    }
  });
  const ratingText = document.getElementById("rating-text-label");
  if (ratingText) {
    const labels = ["", "1 - Poor", "2 - Fair", "3 - Good", "4 - Very Good", "5 - Excellent!"];
    ratingText.textContent = labels[rating] || "";
  }
}

// Load turfs customer can review
async function loadCustomerTurfsForReview(userId) {
  const select = document.getElementById("review-turf-select");
  if (!select) return;

  try {
    // Check if turfId is in URL
    const params = new URLSearchParams(window.location.search);
    const preselectedTurfId = params.get("turfId");

    const res = await fetch(`${API_BASE_URL}/turfs`);
    if (!res.ok) return;
    const turfs = await res.json();

    select.innerHTML = `<option value="">-- Choose a Turf Arena --</option>` +
      turfs.map(t => `
        <option value="${t.id}" ${preselectedTurfId && preselectedTurfId == t.id ? 'selected' : ''}>
          ${t.name} (${t.location})
        </option>
      `).join("");

  } catch (e) {
    console.error(e);
  }
}

async function submitReview(e) {
  e.preventDefault();
  const user = getCurrentUser();
  const turfId = document.getElementById("review-turf-select").value;
  const comment = document.getElementById("review-comment").value.trim();

  if (!turfId) {
    alert("Please select a turf arena to review.");
    return;
  }
  if (!comment) {
    alert("Please write a short comment about your experience.");
    return;
  }

  const payload = {
    userId: user.id,
    turfId: parseInt(turfId, 10),
    rating: selectedRatingValue,
    comment: comment
  };

  try {
    const res = await fetch(`${API_BASE_URL}/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Thank you! Your review has been submitted.", "success");
      document.getElementById("review-comment").value = "";
      loadCustomerReviewsList(user.id);
    } else {
      showToast(data.message || "Failed to submit review", "danger");
    }
  } catch (err) {
    console.error(err);
    showToast("Network error submitting review.", "danger");
  }
}

// Load reviews written by customer
async function loadCustomerReviewsList(userId) {
  const container = document.getElementById("my-reviews-container");
  if (!container) return;

  try {
    const res = await fetch(`${API_BASE_URL}/reviews`);
    if (!res.ok) return;
    const allReviews = await res.json();
    const myReviews = allReviews.filter(r => r.user && r.user.id === userId);

    if (myReviews.length === 0) {
      container.innerHTML = `
        <div class="text-center py-4 text-muted border rounded bg-white">
          <i class="bi bi-chat-square-quote fs-2 text-secondary"></i>
          <p class="mt-2 mb-0">You haven't submitted any reviews yet.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = myReviews.map(r => `
      <div class="card border-0 shadow-sm mb-3">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <h6 class="fw-bold text-dark mb-0">${r.turf ? r.turf.name : 'Turf Arena'}</h6>
            <div class="star-rating">${"★".repeat(r.rating)}${"☆".repeat(5 - r.rating)}</div>
          </div>
          <p class="text-secondary small mb-1">${r.comment}</p>
          <small class="text-muted" style="font-size: 0.75rem;">Submitted on: ${new Date(r.createdAt).toLocaleString()}</small>
        </div>
      </div>
    `).join("");

  } catch (e) {
    console.error(e);
  }
}
