/**
 * registrations.js — My Registrations & Digital Ticket Pass logic
 * Campus Event Registration System
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireAuth('login.html')) return;
  if (Auth.isAdmin()) {
    window.location.href = 'admin/index.html';
    return;
  }

  initMyRegistrations();
});

let allRegistrations = [];
let activeTab = 'all';

async function initMyRegistrations() {
  const tabs = document.querySelectorAll('.reg-tab-btn');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      activeTab = tab.getAttribute('data-tab') || 'all';
      renderRegistrationsList();
    });
  });

  loadRegistrations();
}

async function loadRegistrations() {
  const container = document.getElementById('my-registrations-container');
  if (!container) return;

  try {
    const res = await api.get('/api/registrations/my');
    allRegistrations = res?.data || [];
  } catch (err) {
    const user = Auth.getUser() || { name: 'Alex Chen', department: 'Computer Science' };
    allRegistrations = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    if (allRegistrations.length === 0) {
      allRegistrations = [
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
      localStorage.setItem('mock_registrations', JSON.stringify(allRegistrations));
    }
  }

  renderRegistrationsList();
}

function renderRegistrationsList() {
  const container = document.getElementById('my-registrations-container');
  if (!container) return;

  let filtered = allRegistrations;
  if (activeTab === 'confirmed') {
    filtered = allRegistrations.filter(r => r.status === 'confirmed');
  } else if (activeTab === 'cancelled') {
    filtered = allRegistrations.filter(r => r.status === 'cancelled');
  }

  const countBadge = document.getElementById('reg-count-badge');
  if (countBadge) countBadge.textContent = `${filtered.length}`;

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 60px 20px;">
        <div class="empty-state-icon">🎫</div>
        <h3>No ${activeTab === 'all' ? '' : activeTab} registrations found</h3>
        <p>You haven't signed up for any events in this section yet.</p>
        <a href="events.html" class="btn btn-primary" style="margin-top: 16px;">Explore Campus Events</a>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(reg => {
    const isConfirmed = reg.status === 'confirmed';
    const regCode = `EVT-${(reg.eventId || 1).toString().padStart(3, '0')}-REG${(reg.id || 101).toString().padStart(4, '0')}`;

    return `
      <div class="ticket-card ${!isConfirmed ? 'ticket-cancelled' : ''} animate-fade-in">
        <div class="ticket-left">
          <div class="ticket-status-pill ${isConfirmed ? 'pill-confirmed' : 'pill-cancelled'}">
            ${isConfirmed ? '✓ Confirmed Pass' : '✕ Cancelled'}
          </div>
          <h3 class="ticket-title">${escapeHtml(reg.eventTitle || 'Campus Event')}</h3>
          
          <div class="ticket-details-grid">
            <div class="ticket-info-block">
              <span class="info-label">DATE & TIME</span>
              <span class="info-value">📅 ${formatDate(reg.eventDate)}</span>
              ${reg.eventTime ? `<span class="info-sub">⏰ ${formatTime(reg.eventTime)}</span>` : ''}
            </div>

            <div class="ticket-info-block">
              <span class="info-label">VENUE</span>
              <span class="info-value">📍 ${escapeHtml(reg.eventVenue || 'Main Auditorium')}</span>
            </div>

            <div class="ticket-info-block">
              <span class="info-label">ATTENDEE</span>
              <span class="info-value">👤 ${escapeHtml(reg.studentName || Auth.getUser()?.name || 'Student')}</span>
              <span class="info-sub">${escapeHtml(reg.department || Auth.getUser()?.department || '')}</span>
            </div>

            <div class="ticket-info-block">
              <span class="info-label">PASS CODE</span>
              <span class="info-value font-mono">${regCode}</span>
            </div>
          </div>
        </div>

        <div class="ticket-right">
          <div class="ticket-qr-mockup" style="display: flex; flex-direction: column; align-items: center; gap: 6px;">
            <div class="qr-box">
              <svg viewBox="0 0 100 100" width="80" height="80">
                <rect width="100" height="100" fill="white"/>
                <rect x="10" y="10" width="30" height="30" fill="black"/>
                <rect x="15" y="15" width="20" height="20" fill="white"/>
                <rect x="20" y="20" width="10" height="10" fill="black"/>
                <rect x="60" y="10" width="30" height="30" fill="black"/>
                <rect x="65" y="15" width="20" height="20" fill="white"/>
                <rect x="70" y="20" width="10" height="10" fill="black"/>
                <rect x="10" y="60" width="30" height="30" fill="black"/>
                <rect x="15" y="65" width="20" height="20" fill="white"/>
                <rect x="20" y="70" width="10" height="10" fill="black"/>
                <rect x="50" y="50" width="10" height="10" fill="black"/>
                <rect x="70" y="60" width="15" height="15" fill="black"/>
                <rect x="50" y="75" width="20" height="10" fill="black"/>
              </svg>
            </div>
            <span class="qr-caption">Scan at Entry</span>
          </div>

          <div class="ticket-actions">
            <a href="event-details.html?id=${reg.eventId}" class="btn btn-outline btn-xs">Event Info</a>
            ${isConfirmed ? `
              <button class="btn btn-outline-danger btn-xs" onclick="handleCancelPass(${reg.id}, '${escapeHtml(reg.eventTitle)}')">Cancel</button>
            ` : ''}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

async function handleCancelPass(regId, title) {
  if (!confirm(`Are you sure you want to cancel your registration for "${title}"?`)) return;

  try {
    await api.delete(`/api/registrations/${regId}`);
    showToast('Registration cancelled.', 'info');
  } catch (err) {
    const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    const target = localRegs.find(r => r.id === regId);
    if (target) target.status = 'cancelled';
    localStorage.setItem('mock_registrations', JSON.stringify(localRegs));
    showToast('Registration cancelled.', 'info');
  }

  loadRegistrations();
}
