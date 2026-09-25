/**
 * Smart Turf Booking & Management System
 * Authentication & Session Management
 */

// Retrieve currently logged-in user from localStorage
function getCurrentUser() {
  const userStr = localStorage.getItem("smartturf_user");
  if (!userStr) return null;
  try {
    return JSON.parse(userStr);
  } catch (e) {
    localStorage.removeItem("smartturf_user");
    return null;
  }
}

// Save user session (excluding sensitive data)
function setCurrentUser(user) {
  const safeUser = {
    id: user.id,
    name: user.name,
    email: user.email,
    phone: user.phone,
    role: user.role
  };
  localStorage.setItem("smartturf_user", JSON.stringify(safeUser));
}

// Check if user is logged in
function isLoggedIn() {
  return getCurrentUser() !== null;
}

// Check if user is Admin
function isAdmin() {
  const user = getCurrentUser();
  return user !== null && user.role === "ADMIN";
}

// Check if user is Turf Owner
function isOwner() {
  const user = getCurrentUser();
  return user !== null && user.role === "OWNER";
}

// Route guard: require login and role check
function requireAuth(requiredRole = null) {
  const user = getCurrentUser();
  const isAdminPath = window.location.pathname.includes("/admin/");
  const prefix = isAdminPath ? "../" : "";

  if (!user) {
    alert("Please log in to continue.");
    window.location.href = isAdminPath ? "login.html" : "login.html";
    return false;
  }

  if (requiredRole) {
    if (requiredRole === "ADMIN" && user.role !== "ADMIN") {
      alert("Access Denied: Administrator privileges required.");
      window.location.href = prefix + "dashboard.html";
      return false;
    }
    if (requiredRole === "CUSTOMER" && user.role !== "CUSTOMER" && user.role !== "USER" && user.role !== "ADMIN") {
      alert("Access Denied: You do not have permissions for this page.");
      window.location.href = prefix + "login.html";
      return false;
    }
  }
  return true;
}

// Logout
function logout() {
  localStorage.removeItem("smartturf_user");
  const prefix = window.location.pathname.includes("/admin/") ? "../" : "";
  window.location.href = prefix + "login.html";
}

// Show feedback toast or alert banner
function showToast(message, type = "success") {
  let toastContainer = document.getElementById("toast-container");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.id = "toast-container";
    toastContainer.className = "position-fixed top-0 end-0 p-3";
    toastContainer.style.zIndex = "1100";
    document.body.appendChild(toastContainer);
  }

  const bgClass = type === "success" ? "bg-success text-white" :
                  type === "danger" ? "bg-danger text-white" :
                  type === "warning" ? "bg-warning text-dark" : "bg-primary text-white";

  const toastEl = document.createElement("div");
  toastEl.className = `toast align-items-center ${bgClass} border-0 show mb-2`;
  toastEl.setAttribute("role", "alert");
  toastEl.innerHTML = `
    <div class="d-flex">
      <div class="toast-body fw-semibold">${message}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>
  `;

  toastContainer.appendChild(toastEl);
  setTimeout(() => {
    toastEl.remove();
  }, 4000);
}

// Fetch unread notification count
async function updateNotificationBadge() {
  const user = getCurrentUser();
  if (!user) return;
  try {
    const res = await fetch(`${API_BASE_URL}/notifications/user/${user.id}/unread-count`);
    if (res.ok) {
      const data = await res.json();
      const badge = document.getElementById("nav-unread-badge");
      if (badge) {
        if (data.unreadCount > 0) {
          badge.textContent = data.unreadCount;
          badge.classList.remove("d-none");
        } else {
          badge.classList.add("d-none");
        }
      }
    }
  } catch (err) {
    console.warn("Could not load notification count:", err);
  }
}

// Initialize navbar state on DOM load
document.addEventListener("DOMContentLoaded", () => {
  const user = getCurrentUser();
  const authNav = document.getElementById("auth-nav-items");
  if (authNav) {
    if (user) {
      const dashboardLink = user.role === "ADMIN" ? "admin/dashboard.html" : "dashboard.html";
      authNav.innerHTML = `
        <li class="nav-item">
          <a class="nav-link" href="${dashboardLink}">
            <i class="bi bi-speedometer2 me-1"></i>Dashboard
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link position-relative" href="dashboard.html#notifications">
            <i class="bi bi-bell me-1"></i>Alerts
            <span id="nav-unread-badge" class="badge rounded-pill bg-danger position-absolute top-0 start-100 translate-middle d-none">0</span>
          </a>
        </li>
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle text-white fw-bold" href="#" role="button" data-bs-toggle="dropdown">
            <i class="bi bi-person-circle me-1"></i>${user.name} (${user.role})
          </a>
          <ul class="dropdown-menu dropdown-menu-end shadow">
            <li><a class="dropdown-item" href="${dashboardLink}"><i class="bi bi-person me-2"></i>My Dashboard</a></li>
            ${user.role === 'CUSTOMER' ? `
              <li><a class="dropdown-item" href="bookings.html"><i class="bi bi-calendar-check me-2"></i>My Bookings</a></li>
              <li><a class="dropdown-item" href="complaints.html"><i class="bi bi-ticket-perforated me-2"></i>Complaints</a></li>
            ` : `
              <li><a class="dropdown-item" href="admin/turfs.html"><i class="bi bi-grid me-2"></i>Manage Turfs</a></li>
              <li><a class="dropdown-item" href="admin/bookings.html"><i class="bi bi-journal-text me-2"></i>Manage Bookings</a></li>
            `}
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
          </ul>
        </li>
      `;
      updateNotificationBadge();
    } else {
      authNav.innerHTML = `
        <li class="nav-item">
          <a class="nav-link" href="login.html"><i class="bi bi-box-arrow-in-right me-1"></i>Login</a>
        </li>
        <li class="nav-item ms-lg-2">
          <a class="btn btn-primary-turf btn-sm" href="register.html">Register</a>
        </li>
      `;
    }
  }
});
