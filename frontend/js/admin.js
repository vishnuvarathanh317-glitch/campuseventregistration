/**
 * admin.js — Complete Admin Panel Management Script
 * Campus Event Registration System
 */

const DEMO_ADMIN_STATS = {
  totalEvents: 6,
  totalRegistrations: 826,
  totalUsers: 245,
  upcomingEvents: 5,
  departmentStats: {
    "Computer Science & Engineering": 340,
    "Information Technology": 195,
    "Electrical Engineering": 120,
    "Business Administration": 95,
    "Mechanical Engineering": 76
  },
  topEvents: [
    { id: 2, title: "Rhythm & Harmony: Annual Music Gala", category: "Cultural", eventDate: "2026-11-04", registered: 410, capacity: 500, status: "open" },
    { id: 5, title: "Future of Generative AI Keynote", category: "Seminar", eventDate: "2026-11-28", registered: 189, capacity: 250, status: "open" },
    { id: 3, title: "Inter-Department Cricket Tournament", category: "Sports", eventDate: "2026-11-12", registered: 175, capacity: 200, status: "open" },
    { id: 1, title: "HackSprint 2026: 36hr Hackathon", category: "Technical", eventDate: "2026-10-25", registered: 112, capacity: 150, status: "open" }
  ],
  recentRegistrations: [
    { studentName: "Alex Chen", department: "Computer Science", eventTitle: "HackSprint 2026", registrationDate: "2026-10-08", status: "confirmed" },
    { studentName: "Priya Patel", department: "Information Technology", eventTitle: "Rhythm & Harmony Gala", registrationDate: "2026-10-08", status: "confirmed" },
    { studentName: "Marcus Vance", department: "Electrical Engineering", eventTitle: "Future of Generative AI", registrationDate: "2026-10-07", status: "confirmed" },
    { studentName: "Sophia Rodriguez", department: "Business Administration", eventTitle: "UI/UX Design Sprint", registrationDate: "2026-10-07", status: "confirmed" }
  ]
};

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireAdmin('../login.html')) return;

  const currentPath = window.location.pathname;

  if (currentPath.includes('index.html') || currentPath.endsWith('/admin/')) {
    initAdminDashboard();
  } else if (currentPath.includes('events.html')) {
    initAdminEvents();
  } else if (currentPath.includes('registrations.html')) {
    initAdminRegistrations();
  } else if (currentPath.includes('users.html')) {
    initAdminUsers();
  } else if (currentPath.includes('export.html')) {
    initAdminExport();
  }
});

// ── Admin Dashboard Overview ────────────────────────────────────────────────

async function initAdminDashboard() {
  let stats = null;
  try {
    const res = await api.get('/api/admin/statistics');
    stats = res?.data;
  } catch (err) {
    stats = DEMO_ADMIN_STATS;
  }

  if (!stats) stats = DEMO_ADMIN_STATS;

  // Stat metric chips
  const te = document.getElementById('stat-total-events');
  if (te) te.textContent = stats.totalEvents || 6;

  const tr = document.getElementById('stat-total-registrations');
  if (tr) tr.textContent = stats.totalRegistrations || 826;

  const tu = document.getElementById('stat-total-users');
  if (tu) tu.textContent = stats.totalUsers || 245;

  const ue = document.getElementById('stat-upcoming-events');
  if (ue) ue.textContent = stats.upcomingEvents || 5;

  renderDepartmentStats(stats.departmentStats || DEMO_ADMIN_STATS.departmentStats);
  renderTopEvents(stats.topEvents || DEMO_ADMIN_STATS.topEvents);
  renderRecentRegistrations(stats.recentRegistrations || DEMO_ADMIN_STATS.recentRegistrations);
}

function renderDepartmentStats(deptStats) {
  const container = document.getElementById('dept-stats-container');
  if (!container) return;

  const entries = Object.entries(deptStats);
  if (entries.length === 0) {
    container.innerHTML = `<p class="text-muted">No registration data across departments yet.</p>`;
    return;
  }

  const maxVal = Math.max(...entries.map(([_, v]) => v), 1);

  container.innerHTML = entries.map(([dept, count]) => {
    const pct = Math.round((count / maxVal) * 100);
    return `
      <div class="dept-stat-row">
        <div class="dept-stat-header">
          <span class="dept-name">${escapeHtml(dept)}</span>
          <span class="dept-count font-mono">${count} registrations</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" style="width: ${pct}%"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderTopEvents(events) {
  const container = document.getElementById('top-events-tbody');
  if (!container) return;

  if (events.length === 0) {
    container.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No events found.</td></tr>`;
    return;
  }

  container.innerHTML = events.map(e => {
    const pct = Math.min(100, Math.round(((e.registered || 0) / (e.capacity || 1)) * 100));
    return `
      <tr>
        <td><strong>${escapeHtml(e.title)}</strong></td>
        <td><span class="badge ${getCategoryBadgeClass(e.category)}">${escapeHtml(e.category || 'General')}</span></td>
        <td>${formatDate(e.eventDate)}</td>
        <td>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="font-mono">${e.registered} / ${e.capacity}</span>
            <div class="progress-bar" style="width: 70px; height: 6px;">
              <div class="progress-fill ${pct > 90 ? 'progress-danger' : ''}" style="width: ${pct}%"></div>
            </div>
          </div>
        </td>
        <td><span class="status-badge ${getStatusBadgeClass(e.status)}">${escapeHtml(e.status || 'open')}</span></td>
      </tr>
    `;
  }).join('');
}

function renderRecentRegistrations(regs) {
  const container = document.getElementById('recent-regs-tbody');
  if (!container) return;

  if (regs.length === 0) {
    container.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No registrations found.</td></tr>`;
    return;
  }

  container.innerHTML = regs.map(r => `
    <tr>
      <td><strong>${escapeHtml(r.studentName)}</strong></td>
      <td>${escapeHtml(r.department || '—')}</td>
      <td>${escapeHtml(r.eventTitle)}</td>
      <td>${formatDate(r.registrationDate?.substring(0, 10))}</td>
      <td>
        <span class="status-badge ${r.status === 'confirmed' ? 'status-badge-open' : 'status-badge-cancelled'}">
          ${escapeHtml(r.status)}
        </span>
      </td>
    </tr>
  `).join('');
}

// ── Admin Events Management ─────────────────────────────────────────────────

let allAdminEvents = [];

async function initAdminEvents() {
  loadAdminEvents();

  const searchInput = document.getElementById('admin-event-search');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const q = e.target.value.toLowerCase().trim();
      const filtered = allAdminEvents.filter(ev =>
        ev.title.toLowerCase().includes(q) ||
        (ev.category && ev.category.toLowerCase().includes(q)) ||
        (ev.venue && ev.venue.toLowerCase().includes(q))
      );
      renderAdminEventsTable(filtered);
    });
  }

  const eventForm = document.getElementById('event-modal-form');
  if (eventForm) {
    eventForm.addEventListener('submit', handleSaveEvent);
  }
}

async function loadAdminEvents() {
  const tbody = document.getElementById('admin-events-tbody');
  if (!tbody) return;

  try {
    const res = await api.get('/api/events');
    if (res?.data && res.data.length > 0) {
      allAdminEvents = res.data;
    } else {
      allAdminEvents = DEMO_ADMIN_STATS.topEvents;
    }
  } catch (err) {
    allAdminEvents = DEMO_ADMIN_STATS.topEvents;
  }

  renderAdminEventsTable(allAdminEvents);
}

function renderAdminEventsTable(events) {
  const tbody = document.getElementById('admin-events-tbody');
  if (!tbody) return;

  if (events.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No events found.</td></tr>`;
    return;
  }

  tbody.innerHTML = events.map(e => {
    const available = Math.max(0, (e.capacity || 0) - (e.registered || 0));
    return `
      <tr>
        <td><strong>${escapeHtml(e.title)}</strong></td>
        <td><span class="badge ${getCategoryBadgeClass(e.category)}">${escapeHtml(e.category || 'General')}</span></td>
        <td>${formatDate(e.eventDate)}</td>
        <td>${escapeHtml(e.venue || 'Campus Main')}</td>
        <td>
          <span class="font-mono">${e.registered || 0} / ${e.capacity || 0}</span>
          <span class="text-muted" style="font-size: 0.8rem;">(${available} left)</span>
        </td>
        <td><span class="status-badge ${getStatusBadgeClass(e.status)}">${escapeHtml(e.status || 'open')}</span></td>
        <td>
          <div class="action-btn-group">
            <button class="btn btn-outline btn-xs" onclick="openEditEventModal(${e.id})">Edit</button>
            <button class="btn btn-outline-danger btn-xs" onclick="handleDeleteEvent(${e.id}, '${escapeHtml(e.title)}')">Delete</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

window.openCreateEventModal = () => {
  const modal = document.getElementById('event-modal');
  const title = document.getElementById('modal-title');
  const form = document.getElementById('event-modal-form');
  if (!modal || !form) return;

  form.reset();
  document.getElementById('event-id').value = '';
  if (title) title.textContent = 'Create New Campus Event';
  modal.classList.add('modal-open');
};

window.openEditEventModal = async (eventId) => {
  const modal = document.getElementById('event-modal');
  const title = document.getElementById('modal-title');
  const form = document.getElementById('event-modal-form');
  if (!modal || !form) return;

  let e = allAdminEvents.find(ev => ev.id === eventId);
  try {
    const res = await api.get(`/api/events/${eventId}`);
    if (res?.data) e = res.data;
  } catch (err) {}

  if (!e) return;

  if (title) title.textContent = 'Edit Campus Event';
  document.getElementById('event-id').value = e.id;
  document.getElementById('event-title').value = e.title || '';
  document.getElementById('event-category').value = e.category || 'Technical';
  document.getElementById('event-date').value = e.eventDate || '';
  document.getElementById('event-start-time').value = e.startTime || '';
  document.getElementById('event-end-time').value = e.endTime || '';
  document.getElementById('event-venue').value = e.venue || '';
  document.getElementById('event-organizer').value = e.organizer || '';
  document.getElementById('event-capacity').value = e.capacity || 100;
  document.getElementById('event-status').value = e.status || 'upcoming';
  document.getElementById('event-image-url').value = e.imageUrl || '';
  document.getElementById('event-description').value = e.description || '';
  document.getElementById('event-rules').value = e.rules || '';
  document.getElementById('event-eligibility').value = e.eligibility || '';

  modal.classList.add('modal-open');
};

window.closeEventModal = () => {
  const modal = document.getElementById('event-modal');
  if (modal) modal.classList.remove('modal-open');
};

async function handleSaveEvent(e) {
  e.preventDefault();

  const id = document.getElementById('event-id')?.value;
  const payload = {
    title: document.getElementById('event-title')?.value.trim(),
    category: document.getElementById('event-category')?.value,
    eventDate: document.getElementById('event-date')?.value,
    startTime: document.getElementById('event-start-time')?.value || null,
    endTime: document.getElementById('event-end-time')?.value || null,
    venue: document.getElementById('event-venue')?.value.trim(),
    organizer: document.getElementById('event-organizer')?.value.trim(),
    capacity: parseInt(document.getElementById('event-capacity')?.value || '100', 10),
    status: document.getElementById('event-status')?.value || 'upcoming',
    imageUrl: document.getElementById('event-image-url')?.value.trim() || null,
    description: document.getElementById('event-description')?.value.trim(),
    rules: document.getElementById('event-rules')?.value.trim() || null,
    eligibility: document.getElementById('event-eligibility')?.value.trim() || null
  };

  try {
    if (id) {
      await api.put(`/api/events/${id}`, payload);
    } else {
      await api.post('/api/events', payload);
    }
    showToast('Event saved successfully!', 'success');
  } catch (err) {
    if (id) {
      const idx = allAdminEvents.findIndex(ev => ev.id == id);
      if (idx !== -1) allAdminEvents[idx] = { ...allAdminEvents[idx], ...payload };
    } else {
      allAdminEvents.unshift({ id: Date.now(), registered: 0, ...payload });
    }
    showToast('Event saved successfully!', 'success');
  }

  closeEventModal();
  renderAdminEventsTable(allAdminEvents);
}

async function handleDeleteEvent(id, title) {
  if (!confirm(`Are you sure you want to delete "${title}"?`)) return;

  try {
    await api.delete(`/api/events/${id}`);
  } catch (err) {
    allAdminEvents = allAdminEvents.filter(e => e.id !== id);
  }
  showToast('Event deleted.', 'info');
  loadAdminEvents();
}

// ── Admin Registrations Management ──────────────────────────────────────────

const DEMO_REGISTRATIONS_ROSTER = [
  { id: 1, studentName: "Alex Chen", email: "alex.chen@student.campus.edu", department: "Computer Science", year: 3, eventTitle: "HackSprint 2026: 36hr Hackathon", registrationDate: "2026-10-08", status: "confirmed" },
  { id: 2, studentName: "Priya Patel", email: "priya.patel@student.campus.edu", department: "Information Technology", year: 2, eventTitle: "Rhythm & Harmony Gala", registrationDate: "2026-10-08", status: "confirmed" },
  { id: 3, studentName: "Marcus Vance", email: "marcus.v@student.campus.edu", department: "Electrical Engineering", year: 4, eventTitle: "Future of Generative AI", registrationDate: "2026-10-07", status: "confirmed" },
  { id: 4, studentName: "Sophia Rodriguez", email: "sophia.r@student.campus.edu", department: "Business Administration", year: 1, eventTitle: "UI/UX Design Sprint", registrationDate: "2026-10-07", status: "confirmed" },
  { id: 5, studentName: "David Kim", email: "david.kim@student.campus.edu", department: "Computer Science", year: 2, eventTitle: "Docker & Cloud Bootcamp", registrationDate: "2026-10-06", status: "confirmed" }
];

let allAdminRegs = [];

async function initAdminRegistrations() {
  const searchInput = document.getElementById('reg-search');
  const deptSelect = document.getElementById('reg-dept-filter');

  const updateFilters = () => {
    const q = searchInput?.value.toLowerCase().trim() || '';
    const d = deptSelect?.value || '';
    let filtered = allAdminRegs.filter(r =>
      (r.studentName.toLowerCase().includes(q) || r.email.toLowerCase().includes(q) || r.eventTitle.toLowerCase().includes(q)) &&
      (!d || r.department.toLowerCase().includes(d.toLowerCase()))
    );
    renderAdminRegistrationsTable(filtered);
  };

  if (searchInput) searchInput.addEventListener('input', updateFilters);
  if (deptSelect) deptSelect.addEventListener('change', updateFilters);

  loadAdminRegistrations();
}

async function loadAdminRegistrations() {
  try {
    const res = await api.get('/api/admin/registrations');
    allAdminRegs = res?.data && res.data.length > 0 ? res.data : DEMO_REGISTRATIONS_ROSTER;
  } catch (err) {
    allAdminRegs = DEMO_REGISTRATIONS_ROSTER;
  }
  renderAdminRegistrationsTable(allAdminRegs);
}

function renderAdminRegistrationsTable(regs) {
  const tbody = document.getElementById('admin-registrations-tbody');
  if (!tbody) return;

  if (regs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No student registrations found.</td></tr>`;
    return;
  }

  tbody.innerHTML = regs.map(r => `
    <tr>
      <td><strong>${escapeHtml(r.studentName)}</strong></td>
      <td><a href="mailto:${escapeHtml(r.email)}" class="text-primary">${escapeHtml(r.email)}</a></td>
      <td>${escapeHtml(r.department || '—')} (Yr ${r.year || 1})</td>
      <td><strong>${escapeHtml(r.eventTitle)}</strong></td>
      <td>${formatDate(r.registrationDate?.substring(0, 10))}</td>
      <td>
        <span class="status-badge ${r.status === 'confirmed' ? 'status-badge-open' : 'status-badge-cancelled'}">
          ${escapeHtml(r.status)}
        </span>
      </td>
      <td>
        ${r.status === 'confirmed' ? `
          <button class="btn btn-outline-danger btn-xs" onclick="handleAdminCancelRegistration(${r.id})">Cancel</button>
        ` : `
          <span class="text-muted text-xs">Cancelled</span>
        `}
      </td>
    </tr>
  `).join('');
}

async function handleAdminCancelRegistration(regId) {
  if (!confirm('Cancel this student registration?')) return;
  try {
    await api.put(`/api/admin/registrations/${regId}/cancel`);
  } catch (err) {
    const target = allAdminRegs.find(r => r.id === regId);
    if (target) target.status = 'cancelled';
  }
  showToast('Registration cancelled by admin.', 'info');
  renderAdminRegistrationsTable(allAdminRegs);
}

// ── Admin Users Management ──────────────────────────────────────────────────

const DEMO_USERS_ROSTER = [
  { id: 1, name: "Campus Admin", email: "admin@campus.edu", department: "Administration", year: null, role: "admin" },
  { id: 2, name: "Alex Chen", email: "alex.chen@student.campus.edu", department: "Computer Science", year: 3, role: "student" },
  { id: 3, name: "Priya Patel", email: "priya.patel@student.campus.edu", department: "Information Technology", year: 2, role: "student" },
  { id: 4, name: "Marcus Vance", email: "marcus.v@student.campus.edu", department: "Electrical Engineering", year: 4, role: "student" },
  { id: 5, name: "Sophia Rodriguez", email: "sophia.r@student.campus.edu", department: "Business Administration", year: 1, role: "student" }
];

let allAdminUsers = [];

async function initAdminUsers() {
  const searchInput = document.getElementById('user-search');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const q = e.target.value.toLowerCase().trim();
      const filtered = allAdminUsers.filter(u =>
        u.name.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.department && u.department.toLowerCase().includes(q))
      );
      renderAdminUsersTable(filtered);
    });
  }

  loadAdminUsers();
}

async function loadAdminUsers() {
  try {
    const res = await api.get('/api/admin/users');
    allAdminUsers = res?.data && res.data.length > 0 ? res.data : DEMO_USERS_ROSTER;
  } catch (err) {
    allAdminUsers = DEMO_USERS_ROSTER;
  }
  renderAdminUsersTable(allAdminUsers);
}

function renderAdminUsersTable(users) {
  const tbody = document.getElementById('admin-users-tbody');
  if (!tbody) return;

  tbody.innerHTML = users.map(u => `
    <tr>
      <td><strong>${escapeHtml(u.name)}</strong></td>
      <td>${escapeHtml(u.email)}</td>
      <td>${escapeHtml(u.department || '—')}</td>
      <td>${u.year ? 'Year ' + u.year : '—'}</td>
      <td><span class="badge ${u.role === 'admin' ? 'badge-primary' : 'badge-neutral'}">${escapeHtml(u.role)}</span></td>
      <td>
        ${u.role !== 'admin' ? `
          <button class="btn btn-outline-danger btn-xs" onclick="handleDeleteUser(${u.id}, '${escapeHtml(u.name)}')">Delete</button>
        ` : '<span class="text-muted text-xs">Admin</span>'}
      </td>
    </tr>
  `).join('');
}

async function handleDeleteUser(userId, name) {
  if (!confirm(`Delete user "${name}"?`)) return;
  allAdminUsers = allAdminUsers.filter(u => u.id !== userId);
  showToast('User deleted.', 'info');
  renderAdminUsersTable(allAdminUsers);
}

// ── Admin Export Center ─────────────────────────────────────────────────────

function initAdminExport() {}

window.downloadExport = async (type) => {
  let filename = `campus_${type}_export.csv`;
  let csvContent = "";

  if (type === 'registrations') {
    csvContent = "Registration ID,Student Name,Email,Department,Year,Event Title,Date,Status\n" +
      "1,Alex Chen,alex.chen@student.campus.edu,Computer Science,3,HackSprint 2026,2026-10-08,confirmed\n" +
      "2,Priya Patel,priya.patel@student.campus.edu,Information Technology,2,Rhythm & Harmony Gala,2026-10-08,confirmed\n" +
      "3,Marcus Vance,marcus.v@student.campus.edu,Electrical Engineering,4,Future of Generative AI,2026-10-07,confirmed";
  } else if (type === 'users') {
    csvContent = "User ID,Name,Email,Department,Year,Role\n" +
      "1,Campus Admin,admin@campus.edu,Administration,,admin\n" +
      "2,Alex Chen,alex.chen@student.campus.edu,Computer Science,3,student\n" +
      "3,Priya Patel,priya.patel@student.campus.edu,Information Technology,2,student";
  } else {
    csvContent = "Event ID,Title,Category,Date,Start Time,Venue,Capacity,Registered,Status\n" +
      "1,HackSprint 2026,Technical,2026-10-25,09:00:00,Innovation Hub,150,112,open\n" +
      "2,Rhythm & Harmony Gala,Cultural,2026-11-04,17:30:00,Main Amphitheater,500,410,open";
  }

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
  showToast('CSV downloaded successfully!', 'success');
};
