/**
 * api.js — Centralized API Client & Utilities
 * Campus Event Registration System
 */

// Auto-detect: use local backend when running on localhost, show notice when deployed
const IS_LOCAL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
const API_BASE = IS_LOCAL ? 'http://localhost:8080' : 'http://localhost:8080'; // Replace with your deployed backend URL

// ── Auth & Session Storage ──────────────────────────────────────────────────

const Auth = {
  getToken() {
    return sessionStorage.getItem('token') || localStorage.getItem('token');
  },
  getUser() {
    try {
      const u = sessionStorage.getItem('user') || localStorage.getItem('user');
      return u ? JSON.parse(u) : null;
    } catch (e) {
      return null;
    }
  },
  setSession(token, user, remember = false) {
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem('token', token);
    storage.setItem('user', JSON.stringify(user));
  },
  clearSession() {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
  isLoggedIn() {
    return !!this.getToken();
  },
  isAdmin() {
    const user = this.getUser();
    return user && user.role === 'admin';
  },
  requireAuth(redirectUrl = 'login.html') {
    if (!this.isLoggedIn()) {
      window.location.href = redirectUrl;
      return false;
    }
    return true;
  },
  requireAdmin(redirectUrl = '../login.html') {
    if (!this.isLoggedIn() || !this.isAdmin()) {
      window.location.href = redirectUrl;
      return false;
    }
    return true;
  },
  redirectIfAuth(studentUrl = 'dashboard.html', adminUrl = 'admin/index.html') {
    if (this.isLoggedIn()) {
      window.location.href = this.isAdmin() ? adminUrl : studentUrl;
    }
  },
  async logout() {
    try {
      const token = this.getToken();
      if (token) {
        await fetch(`${API_BASE}/api/auth/logout`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` }
        });
      }
    } catch (e) {
      console.warn('Logout API failed:', e);
    } finally {
      this.clearSession();
      const inAdmin = window.location.pathname.includes('/admin/');
      window.location.href = inAdmin ? '../login.html' : 'login.html';
    }
  }
};

// ── HTTP Fetch Client ───────────────────────────────────────────────────────

const api = {
  async request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = Auth.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const res = await fetch(url, {
        ...options,
        headers
      });

      // Handle CSV or non-JSON downloads
      const contentType = res.headers.get('content-type') || '';
      if (contentType.includes('text/csv')) {
        const blob = await res.blob();
        return { success: true, blob };
      }

      const json = await res.json().catch(() => null);

      if (!res.ok) {
        // Handle auth expired
        if (res.status === 401 && !endpoint.includes('/login') && !endpoint.includes('/register')) {
          Auth.clearSession();
          showToast('Session expired. Please log in again.', 'error');
          setTimeout(() => {
            const inAdmin = window.location.pathname.includes('/admin/');
            window.location.href = inAdmin ? '../login.html' : 'login.html';
          }, 1200);
          throw new Error('Unauthorized');
        }

        const errMsg = json?.error || json?.message || `Request failed (${res.status})`;
        throw new Error(errMsg);
      }

      return json;
    } catch (err) {
      console.error(`[API Error] ${options.method || 'GET'} ${endpoint}:`, err);
      throw err;
    }
  },

  get(endpoint, params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v);
    });
    const queryString = query.toString() ? `?${query.toString()}` : '';
    return this.request(`${endpoint}${queryString}`, { method: 'GET' });
  },

  post(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  },

  put(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
};

// ── Toast Notification System ───────────────────────────────────────────────

function showToast(message, type = 'info', duration = 3500) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type} animate-slide-in-right`;

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };

  toast.innerHTML = `
    <span class="toast-icon">${icons[type] || 'ℹ'}</span>
    <span class="toast-message">${escapeHtml(message)}</span>
    <button class="toast-close" onclick="this.parentElement.remove()">×</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('animate-fade-out');
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

// ── Utility Helpers ─────────────────────────────────────────────────────────

function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

function formatDate(dateStr) {
  if (!dateStr) return 'TBA';
  try {
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  } catch (e) {
    return dateStr;
  }
}

function formatTime(timeStr) {
  if (!timeStr) return '';
  try {
    const parts = timeStr.split(':');
    const h = parseInt(parts[0], 10);
    const m = parts[1] || '00';
    const ampm = h >= 12 ? 'PM' : 'AM';
    const h12 = h % 12 || 12;
    return `${h12}:${m} ${ampm}`;
  } catch (e) {
    return timeStr;
  }
}

function getCategoryBadgeClass(category) {
  const c = (category || '').toLowerCase();
  if (c.includes('tech') || c.includes('code') || c.includes('hack')) return 'badge-tech';
  if (c.includes('cult') || c.includes('music') || c.includes('art') || c.includes('dance')) return 'badge-cultural';
  if (c.includes('sport') || c.includes('game')) return 'badge-sports';
  if (c.includes('work') || c.includes('boot')) return 'badge-workshop';
  if (c.includes('sem') || c.includes('talk') || c.includes('keynote')) return 'badge-seminar';
  return 'badge-primary';
}

function getStatusBadgeClass(status) {
  const s = (status || '').toLowerCase();
  if (s === 'open') return 'status-badge-open';
  if (s === 'upcoming') return 'status-badge-upcoming';
  if (s === 'full') return 'status-badge-full';
  if (s === 'completed') return 'status-badge-completed';
  if (s === 'cancelled') return 'status-badge-cancelled';
  return 'status-badge-open';
}

function setupGlobalNavigation() {
  const user = Auth.getUser();
  const navContainer = document.getElementById('navbar-auth-section');
  if (!navContainer) return;

  if (user) {
    const dashboardLink = user.role === 'admin' ? 'admin/index.html' : 'dashboard.html';

    navContainer.innerHTML = `
      <div class="user-profile-menu">
        <a href="${dashboardLink}" class="nav-btn nav-btn-outline">
          <span class="user-avatar-initial">${escapeHtml(user.name.charAt(0).toUpperCase())}</span>
          <span>${escapeHtml(user.name.split(' ')[0])}</span>
        </a>
        <button class="nav-btn nav-btn-primary" onclick="Auth.logout()">Logout</button>
      </div>
    `;
  } else {
    navContainer.innerHTML = `
      <a href="login.html" class="nav-btn nav-btn-outline">Log In</a>
      <a href="register.html" class="nav-btn nav-btn-primary">Get Started</a>
    `;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  setupGlobalNavigation();
});
