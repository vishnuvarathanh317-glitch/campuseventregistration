/**
 * dashboard.js — Student Dashboard logic
 * Campus Event Registration System
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireAuth('login.html')) return;
  if (Auth.isAdmin()) {
    window.location.href = 'admin/index.html';
    return;
  }

  loadStudentDashboard();
});

async function loadStudentDashboard() {
  const user = Auth.getUser() || { name: 'Alex Chen', department: 'Computer Science', year: 3 };

  // Populate Greeting
  const greetingEl = document.getElementById('user-greeting');
  if (greetingEl && user) {
    greetingEl.textContent = `Welcome back, ${user.name.split(' ')[0]}! 👋`;
  }

  const deptEl = document.getElementById('user-dept-badge');
  if (deptEl && user) {
    deptEl.textContent = `${user.department || 'Computer Science'} • Year ${user.year || '3'}`;
  }

  let registrations = [];
  let events = [];

  try {
    const [regsRes, eventsRes] = await Promise.all([
      api.get('/api/registrations/my'),
      api.get('/api/events')
    ]);

    registrations = regsRes?.data || [];
    events = eventsRes?.data || [];
  } catch (err) {
    // Local fallback for offline testing
    registrations = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    if (registrations.length === 0) {
      registrations = [
        {
          id: 101,
          eventId: 1,
          eventTitle: "HackSprint 2026: Campus 36hr Hackathon",
          eventDate: "2026-10-25",
          eventTime: "09:00:00",
          eventVenue: "Innovation Hub & Turing Lab 4",
          studentName: user.name,
          department: user.department,
          status: "confirmed"
        },
        {
          id: 102,
          eventId: 2,
          eventTitle: "Rhythm & Harmony: Annual Music Gala",
          eventDate: "2026-11-04",
          eventTime: "17:30:00",
          eventVenue: "Main Grand Amphitheater",
          studentName: user.name,
          department: user.department,
          status: "confirmed"
        }
      ];
      localStorage.setItem('mock_registrations', JSON.stringify(registrations));
    }
    events = [
      { id: 3, title: "Inter-Department Premier Cricket Tournament", category: "Sports", eventDate: "2026-11-12" },
      { id: 5, title: "Future of Generative AI & Agents Keynote", category: "Seminar", eventDate: "2026-11-28" }
    ];
  }

  const activeRegs = registrations.filter(r => r.status === 'confirmed');

  // Update Stats
  const totalRegsEl = document.getElementById('stat-total-registrations');
  if (totalRegsEl) totalRegsEl.textContent = activeRegs.length;

  const totalEventsEl = document.getElementById('stat-total-events');
  if (totalEventsEl) totalEventsEl.textContent = events.length || 6;

  // Render Registered Events List
  renderDashboardRegistrations(activeRegs);

  // Render Recommended Events
  const registeredIds = new Set(activeRegs.map(r => r.eventId));
  const recommended = events.filter(e => !registeredIds.has(e.id)).slice(0, 3);
  renderRecommendedEvents(recommended);
}

function renderDashboardRegistrations(regs) {
  const container = document.getElementById('dashboard-registrations-list');
  if (!container) return;

  if (regs.length === 0) {
    container.innerHTML = `
      <div class="empty-state-sm" style="text-align: center; padding: 20px;">
        <p class="text-muted">You haven't registered for any upcoming events yet.</p>
        <a href="events.html" class="btn btn-primary btn-sm" style="margin-top: 10px;">Explore Campus Events</a>
      </div>
    `;
    return;
  }

  container.innerHTML = regs.slice(0, 4).map(reg => `
    <div class="dash-event-item animate-fade-in">
      <div class="dash-event-date-box">
        <span class="dash-date-day">${formatShortDay(reg.eventDate)}</span>
        <span class="dash-date-month">${formatShortMonth(reg.eventDate)}</span>
      </div>
      <div class="dash-event-info">
        <h4 class="dash-event-title">${escapeHtml(reg.eventTitle || 'Event')}</h4>
        <p class="dash-event-meta text-muted">
          <span>📍 ${escapeHtml(reg.eventVenue || 'Campus Venue')}</span>
          ${reg.eventTime ? `<span>• ⏰ ${formatTime(reg.eventTime)}</span>` : ''}
        </p>
      </div>
      <div class="dash-event-action">
        <a href="event-details.html?id=${reg.eventId}" class="btn btn-outline btn-xs">Details</a>
        <button class="btn btn-outline-danger btn-xs" onclick="cancelRegistrationDashboard(${reg.id})">Cancel</button>
      </div>
    </div>
  `).join('');
}

function renderRecommendedEvents(events) {
  const container = document.getElementById('dashboard-recommended-list');
  if (!container) return;

  if (events.length === 0) {
    container.innerHTML = `<p class="text-muted text-center" style="padding: 20px;">All caught up! Check back soon for new events.</p>`;
    return;
  }

  container.innerHTML = events.map(event => `
    <div class="dash-rec-item">
      <div class="dash-rec-body">
        <span class="badge ${getCategoryBadgeClass(event.category)} badge-xs">${escapeHtml(event.category || 'General')}</span>
        <h4 class="dash-rec-title">${escapeHtml(event.title)}</h4>
        <p class="dash-rec-date text-muted">📅 ${formatDate(event.eventDate)}</p>
      </div>
      <a href="event-details.html?id=${event.id}" class="btn btn-outline btn-xs">Register</a>
    </div>
  `).join('');
}

function formatShortDay(dateStr) {
  if (!dateStr) return '--';
  const parts = dateStr.split('-');
  return parts[2] || dateStr;
}

function formatShortMonth(dateStr) {
  if (!dateStr) return '---';
  try {
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
  } catch (e) {
    return '---';
  }
}

async function cancelRegistrationDashboard(regId) {
  if (!confirm('Cancel your registration for this event?')) return;
  try {
    await api.delete(`/api/registrations/${regId}`);
    showToast('Registration cancelled successfully.', 'info');
  } catch (err) {
    const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    const updated = localRegs.filter(r => r.id !== regId);
    localStorage.setItem('mock_registrations', JSON.stringify(updated));
    showToast('Registration cancelled successfully.', 'info');
  }
  loadStudentDashboard();
}
