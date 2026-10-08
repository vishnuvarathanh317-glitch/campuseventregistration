/**
 * events.js — Event discovery, filtering, details, and registration
 * Campus Event Registration System
 */

// Fallback seed events to ensure UI is instantly previewable even before backend is started
const DEMO_FALLBACK_EVENTS = [
  {
    id: 1,
    title: "HackSprint 2026: Campus 36hr Hackathon",
    description: "Build innovative AI, Web3, and IoT solutions in teams of 2-4. Free food, mentor support, and $5,000 in prizes.",
    category: "Technical",
    eventDate: "2026-10-25",
    startTime: "09:00:00",
    endTime: "21:00:00",
    venue: "Innovation Hub & Turing Lab 4",
    organizer: "ACM & IEEE Student Chapters",
    capacity: 150,
    registered: 112,
    status: "open",
    imageUrl: "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800&auto=format&fit=crop&q=80",
    rules: "Teams of 2 to 4 members. Bring laptops, student IDs, and power strips.",
    eligibility: "Open to all Engineering, CS, and Design students."
  },
  {
    id: 2,
    title: "Rhythm & Harmony: Annual Campus Music & Dance Gala",
    description: "The biggest cultural showcase of the semester featuring battle of the bands, classical fusion, and street dance showdowns.",
    category: "Cultural",
    eventDate: "2026-11-04",
    startTime: "17:30:00",
    endTime: "22:30:00",
    venue: "Main Grand Amphitheater",
    organizer: "Cultural Affairs Council",
    capacity: 500,
    registered: 410,
    status: "open",
    imageUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80",
    rules: "Pass required for admission. Entry closes 30 minutes after start time.",
    eligibility: "Open to all university students and faculty."
  },
  {
    id: 3,
    title: "Inter-Department Premier Cricket & Football Tournament",
    description: "Cheer for your department in the semester championship leagues. Trophies, medals, and best athlete awards.",
    category: "Sports",
    eventDate: "2026-11-12",
    startTime: "08:00:00",
    endTime: "18:00:00",
    venue: "University Sports Complex",
    organizer: "Department of Physical Education",
    capacity: 200,
    registered: 175,
    status: "open",
    imageUrl: "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800&auto=format&fit=crop&q=80",
    rules: "Standard sports attire and valid student badge mandatory.",
    eligibility: "All undergraduate and postgraduate students."
  },
  {
    id: 4,
    title: "Hands-on Full-Stack Cloud & Docker Bootcamp",
    description: "Deep dive into microservices architecture, Docker containerization, CI/CD pipelines, and cloud deployments.",
    category: "Workshop",
    eventDate: "2026-11-18",
    startTime: "10:00:00",
    endTime: "16:00:00",
    venue: "Seminar Hall B & Cloud Lab",
    organizer: "Developer Student Club (DSC)",
    capacity: 80,
    registered: 80,
    status: "full",
    imageUrl: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=80",
    rules: "Bring laptop with Docker Desktop and VS Code installed.",
    eligibility: "Computer Science, IT, and Software Engineering students."
  },
  {
    id: 5,
    title: "Future of Generative AI & Autonomous Agents Keynote",
    description: "Industry leaders discuss the paradigm shift in Agentic AI, large models in production, and emerging career roadmaps.",
    category: "Seminar",
    eventDate: "2026-11-28",
    startTime: "14:00:00",
    endTime: "17:00:00",
    venue: "Kavli Auditorium",
    organizer: "AI & Data Science Society",
    capacity: 250,
    registered: 189,
    status: "open",
    imageUrl: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&auto=format&fit=crop&q=80",
    rules: "Q&A session will follow the panel discussion.",
    eligibility: "Open to all students, researchers, and alumni."
  },
  {
    id: 6,
    title: "Campus UI/UX Design Sprint & Prototyping Battle",
    description: "A fast-paced 8-hour design sprint solving campus usability challenges using Figma. Win cash prizes and design mentorship.",
    category: "Technical",
    eventDate: "2026-12-05",
    startTime: "09:30:00",
    endTime: "17:30:00",
    venue: "Design Lab 2, Media Block",
    organizer: "Design & UX Collective",
    capacity: 60,
    registered: 45,
    status: "open",
    imageUrl: "https://images.unsplash.com/photo-1581291518655-9523c932deb2?w=800&auto=format&fit=crop&q=80",
    rules: "Individual or duo participation. Figma link submission required.",
    eligibility: "All departments welcome."
  }
];

let currentFilters = {
  search: '',
  category: '',
  status: '',
  sortBy: 'date',
  asc: true
};

let userRegisteredEventIds = new Set();
let cachedEventsList = [];

document.addEventListener('DOMContentLoaded', () => {
  const isEventsListPage = document.getElementById('events-grid');
  const isEventDetailPage = document.getElementById('event-detail-container');

  if (isEventsListPage) {
    initEventsExplorer();
  }

  if (isEventDetailPage) {
    initEventDetail();
  }
});

// ── Events Explorer ──────────────────────────────────────────────────────────

async function initEventsExplorer() {
  if (Auth.isLoggedIn()) {
    try {
      const myRegs = await api.get('/api/registrations/my');
      if (myRegs && myRegs.data && Array.isArray(myRegs.data)) {
        myRegs.data.forEach(r => {
          if (r.status === 'confirmed') userRegisteredEventIds.add(r.eventId);
        });
      }
    } catch (e) {
      // Check local storage mock if backend is not started
      const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
      localRegs.forEach(r => {
        if (r.status === 'confirmed') userRegisteredEventIds.add(r.eventId);
      });
    }
  }

  // Setup Search Input
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        currentFilters.search = e.target.value.trim().toLowerCase();
        applyClientFiltersAndRender();
      }, 250);
    });
  }

  // Setup Category Pills
  const catButtons = document.querySelectorAll('.filter-pill[data-category]');
  catButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      catButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentFilters.category = btn.getAttribute('data-category') === 'all' ? '' : btn.getAttribute('data-category');
      applyClientFiltersAndRender();
    });
  });

  // Check URL category query param
  const urlParams = new URLSearchParams(window.location.search);
  const catParam = urlParams.get('category');
  if (catParam) {
    catButtons.forEach(btn => {
      if (btn.getAttribute('data-category')?.toLowerCase() === catParam.toLowerCase()) {
        catButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilters.category = catParam;
      }
    });
  }

  // Setup Status Filter
  const statusSelect = document.getElementById('status-filter');
  if (statusSelect) {
    statusSelect.addEventListener('change', (e) => {
      currentFilters.status = e.target.value;
      applyClientFiltersAndRender();
    });
  }

  // Setup Sort Filter
  const sortSelect = document.getElementById('sort-filter');
  if (sortSelect) {
    sortSelect.addEventListener('change', (e) => {
      const val = e.target.value;
      if (val === 'date_asc') { currentFilters.sortBy = 'date'; currentFilters.asc = true; }
      else if (val === 'date_desc') { currentFilters.sortBy = 'date'; currentFilters.asc = false; }
      else if (val === 'seats') { currentFilters.sortBy = 'seats'; currentFilters.asc = false; }
      else if (val === 'title') { currentFilters.sortBy = 'name'; currentFilters.asc = true; }
      applyClientFiltersAndRender();
    });
  }

  loadEvents();
}

async function loadEvents() {
  const container = document.getElementById('events-grid');
  if (!container) return;

  container.innerHTML = getSkeletonCardsHTML(6);

  try {
    const res = await api.get('/api/events');
    if (res && res.data && Array.isArray(res.data) && res.data.length > 0) {
      cachedEventsList = res.data;
    } else {
      cachedEventsList = DEMO_FALLBACK_EVENTS;
    }
  } catch (err) {
    console.info('Using fallback demo events for display:', err.message);
    cachedEventsList = DEMO_FALLBACK_EVENTS;
  }

  applyClientFiltersAndRender();
}

function applyClientFiltersAndRender() {
  const container = document.getElementById('events-grid');
  if (!container) return;

  let filtered = [...cachedEventsList];

  // Search filter
  if (currentFilters.search) {
    const q = currentFilters.search.toLowerCase();
    filtered = filtered.filter(e =>
      (e.title && e.title.toLowerCase().includes(q)) ||
      (e.description && e.description.toLowerCase().includes(q)) ||
      (e.venue && e.venue.toLowerCase().includes(q)) ||
      (e.organizer && e.organizer.toLowerCase().includes(q))
    );
  }

  // Category filter
  if (currentFilters.category) {
    filtered = filtered.filter(e => e.category && e.category.toLowerCase() === currentFilters.category.toLowerCase());
  }

  // Status filter
  if (currentFilters.status) {
    filtered = filtered.filter(e => e.status && e.status.toLowerCase() === currentFilters.status.toLowerCase());
  }

  // Sort
  filtered.sort((a, b) => {
    if (currentFilters.sortBy === 'date') {
      const diff = new Date(a.eventDate) - new Date(b.eventDate);
      return currentFilters.asc ? diff : -diff;
    }
    if (currentFilters.sortBy === 'seats') {
      const seatsA = (a.capacity || 0) - (a.registered || 0);
      const seatsB = (b.capacity || 0) - (b.registered || 0);
      return currentFilters.asc ? (seatsA - seatsB) : (seatsB - seatsA);
    }
    if (currentFilters.sortBy === 'name') {
      const comp = (a.title || '').localeCompare(b.title || '');
      return currentFilters.asc ? comp : -comp;
    }
    return 0;
  });

  renderEventsGrid(filtered, container);

  const countEl = document.getElementById('events-count');
  if (countEl) {
    countEl.textContent = `${filtered.length} event${filtered.length === 1 ? '' : 's'} available`;
  }
}

function renderEventsGrid(events, container) {
  if (!events || events.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">🔍</div>
        <h3>No events found</h3>
        <p>Try adjusting your search criteria or category filters.</p>
        <button class="btn btn-outline" onclick="resetFilters()">Reset Filters</button>
      </div>
    `;
    return;
  }

  container.innerHTML = events.map(event => {
    const isRegistered = userRegisteredEventIds.has(event.id);
    const availableSeats = Math.max(0, (event.capacity || 0) - (event.registered || 0));
    const percentFilled = Math.min(100, Math.round(((event.registered || 0) / (event.capacity || 1)) * 100));
    const isFull = availableSeats <= 0 || event.status === 'full';
    const isPast = event.status === 'completed';

    const fallbackImg = 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop&q=80';
    const imageUrl = event.imageUrl || fallbackImg;

    return `
      <div class="event-card animate-fade-in">
        <div class="event-card-banner">
          <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(event.title)}" loading="lazy" onerror="this.src='${fallbackImg}'">
          <div class="event-card-category ${getCategoryBadgeClass(event.category)}">${escapeHtml(event.category || 'General')}</div>
          ${isRegistered ? '<div class="event-card-badge-registered">✓ Registered</div>' : ''}
          <div class="event-card-status ${getStatusBadgeClass(event.status)}">${escapeHtml(event.status || 'open')}</div>
        </div>
        <div class="event-card-body">
          <div class="event-card-date">
            <span class="date-icon">📅</span>
            <span>${formatDate(event.eventDate)} ${event.startTime ? '• ' + formatTime(event.startTime) : ''}</span>
          </div>
          <h3 class="event-card-title">${escapeHtml(event.title)}</h3>
          <p class="event-card-venue">
            <span class="venue-icon">📍</span>
            <span>${escapeHtml(event.venue || 'Campus Venue')}</span>
          </p>

          <div class="event-card-capacity-box">
            <div class="capacity-info">
              <span class="capacity-label">${availableSeats} seat${availableSeats === 1 ? '' : 's'} left</span>
              <span class="capacity-total">${event.registered || 0} / ${event.capacity || 0}</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill ${percentFilled > 85 ? 'progress-danger' : ''}" style="width: ${percentFilled}%"></div>
            </div>
          </div>
        </div>

        <div class="event-card-footer">
          <a href="event-details.html?id=${event.id}" class="btn btn-outline btn-sm">View Details</a>
          ${isRegistered ? `
            <button class="btn btn-success btn-sm" disabled>✓ Registered</button>
          ` : isFull ? `
            <button class="btn btn-disabled btn-sm" disabled>Full</button>
          ` : isPast ? `
            <button class="btn btn-disabled btn-sm" disabled>Ended</button>
          ` : `
            <button class="btn btn-primary btn-sm" onclick="quickRegister(${event.id}, '${escapeHtml(event.title)}')">Register Now</button>
          `}
        </div>
      </div>
    `;
  }).join('');
}

function getSkeletonCardsHTML(count = 6) {
  return Array(count).fill(0).map(() => `
    <div class="event-card skeleton-card">
      <div class="skeleton skeleton-img"></div>
      <div class="event-card-body" style="gap: 12px; display: flex; flex-direction: column;">
        <div class="skeleton skeleton-text" style="width: 40%; height: 16px;"></div>
        <div class="skeleton skeleton-title" style="width: 80%; height: 24px;"></div>
        <div class="skeleton skeleton-text" style="width: 60%; height: 16px;"></div>
        <div class="skeleton skeleton-text" style="width: 100%; height: 12px; margin-top: 8px;"></div>
      </div>
    </div>
  `).join('');
}

function resetFilters() {
  currentFilters = { search: '', category: '', status: '', sortBy: 'date', asc: true };
  const searchInput = document.getElementById('search-input');
  if (searchInput) searchInput.value = '';
  const catButtons = document.querySelectorAll('.filter-pill[data-category]');
  catButtons.forEach(b => b.classList.toggle('active', b.getAttribute('data-category') === 'all'));
  applyClientFiltersAndRender();
}

async function quickRegister(eventId, eventTitle) {
  if (!Auth.isLoggedIn()) {
    showToast('Please sign in or create a student account to register.', 'info');
    setTimeout(() => {
      window.location.href = `login.html?redirect=event-details.html?id=${eventId}`;
    }, 1000);
    return;
  }

  try {
    await api.post(`/api/events/${eventId}/register`);
    showToast(`Successfully registered for ${eventTitle}!`, 'success');
    userRegisteredEventIds.add(eventId);
    loadEvents();
  } catch (err) {
    // If backend is offline, save to local preview registration
    const user = Auth.getUser() || { name: 'Alex Chen', department: 'Computer Science' };
    const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    const targetEvt = cachedEventsList.find(e => e.id === eventId) || {};
    localRegs.push({
      id: Date.now(),
      eventId,
      eventTitle: targetEvt.title || eventTitle,
      eventDate: targetEvt.eventDate || '2026-10-25',
      eventTime: targetEvt.startTime || '09:00:00',
      eventVenue: targetEvt.venue || 'Campus Venue',
      studentName: user.name,
      department: user.department,
      status: 'confirmed'
    });
    localStorage.setItem('mock_registrations', JSON.stringify(localRegs));
    userRegisteredEventIds.add(eventId);
    showToast(`Successfully registered for ${eventTitle}!`, 'success');
    applyClientFiltersAndRender();
  }
}

// ── Event Detail Page ────────────────────────────────────────────────────────

let currentEvent = null;

async function initEventDetail() {
  const urlParams = new URLSearchParams(window.location.search);
  const eventId = parseInt(urlParams.get('id') || '1', 10);
  const container = document.getElementById('event-detail-container');
  if (!container) return;

  try {
    const res = await api.get(`/api/events/${eventId}`);
    currentEvent = res?.data;
  } catch (err) {
    // Fallback to demo event
    currentEvent = DEMO_FALLBACK_EVENTS.find(e => e.id === eventId) || DEMO_FALLBACK_EVENTS[0];
  }

  if (!currentEvent) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">❌</div>
        <h2>Event Not Found</h2>
        <a href="events.html" class="btn btn-primary" style="margin-top: 16px;">Back to Events</a>
      </div>
    `;
    return;
  }

  let isRegistered = false;
  let registrationId = null;

  if (Auth.isLoggedIn()) {
    try {
      const myRegs = await api.get('/api/registrations/my');
      const reg = (myRegs?.data || []).find(r => r.eventId === currentEvent.id && r.status === 'confirmed');
      if (reg) {
        isRegistered = true;
        registrationId = reg.id;
      }
    } catch (e) {
      const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
      const reg = localRegs.find(r => r.eventId === currentEvent.id && r.status === 'confirmed');
      if (reg) {
        isRegistered = true;
        registrationId = reg.id;
      }
    }
  }

  renderEventDetail(currentEvent, isRegistered, registrationId, container);
}

function renderEventDetail(event, isRegistered, regId, container) {
  const availableSeats = Math.max(0, (event.capacity || 0) - (event.registered || 0));
  const percentFilled = Math.min(100, Math.round(((event.registered || 0) / (event.capacity || 1)) * 100));
  const fallbackImg = 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1200&auto=format&fit=crop&q=80';
  const imageUrl = event.imageUrl || fallbackImg;

  const isFull = availableSeats <= 0 || event.status === 'full';
  const isPast = event.status === 'completed';

  document.title = `${event.title} — Campus Events`;

  container.innerHTML = `
    <!-- Hero Banner -->
    <div class="event-detail-hero">
      <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(event.title)}" class="hero-bg-img" onerror="this.src='${fallbackImg}'">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <a href="events.html" class="back-link">← Back to All Events</a>
        <div class="hero-tags">
          <span class="badge ${getCategoryBadgeClass(event.category)}">${escapeHtml(event.category || 'General')}</span>
          <span class="status-badge ${getStatusBadgeClass(event.status)}">${escapeHtml(event.status || 'open')}</span>
        </div>
        <h1 class="hero-title">${escapeHtml(event.title)}</h1>
        <p class="hero-subtitle">Organized by <strong>${escapeHtml(event.organizer || 'Campus Student Council')}</strong></p>
      </div>
    </div>

    <!-- Main Content Layout -->
    <div class="event-detail-layout">
      <!-- Left Column: Details -->
      <div class="event-detail-main">
        <div class="detail-card">
          <h2>About This Event</h2>
          <div class="detail-description">${escapeHtml(event.description || 'No description provided.').replace(/\n/g, '<br>')}</div>
        </div>

        ${event.rules ? `
          <div class="detail-card">
            <h2>Rules & Guidelines</h2>
            <div class="detail-description">${escapeHtml(event.rules).replace(/\n/g, '<br>')}</div>
          </div>
        ` : ''}

        ${event.eligibility ? `
          <div class="detail-card">
            <h2>Eligibility</h2>
            <div class="detail-description">${escapeHtml(event.eligibility).replace(/\n/g, '<br>')}</div>
          </div>
        ` : ''}
      </div>

      <!-- Right Column: Quick Info & Registration Widget -->
      <div class="event-detail-sidebar">
        <div class="sticky-sidebar-card">
          <div class="sidebar-meta-list">
            <div class="meta-item">
              <span class="meta-icon">📅</span>
              <div>
                <strong>Date & Time</strong>
                <p>${formatDate(event.eventDate)}</p>
                <p class="text-muted">${formatTime(event.startTime)} ${event.endTime ? '– ' + formatTime(event.endTime) : ''}</p>
              </div>
            </div>

            <div class="meta-item">
              <span class="meta-icon">📍</span>
              <div>
                <strong>Venue</strong>
                <p>${escapeHtml(event.venue || 'Campus Main Auditorium')}</p>
              </div>
            </div>

            <div class="meta-item">
              <span class="meta-icon">🎟️</span>
              <div>
                <strong>Capacity & Seats</strong>
                <p>${availableSeats} remaining of ${event.capacity} total</p>
                <div class="progress-bar" style="margin-top: 8px;">
                  <div class="progress-fill ${percentFilled > 85 ? 'progress-danger' : ''}" style="width: ${percentFilled}%"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="sidebar-actions" style="margin-top: 24px;">
            ${isRegistered ? `
              <div class="registered-banner">
                <span class="check-icon">✓</span>
                <div>
                  <strong>You're Registered!</strong>
                  <p class="text-muted" style="font-size: 0.85rem;">Check your dashboard for your digital ticket.</p>
                </div>
              </div>
              <a href="my-registrations.html" class="btn btn-primary btn-block" style="margin-top: 12px;">View My Ticket</a>
              <button class="btn btn-outline-danger btn-block btn-sm" style="margin-top: 8px;" onclick="cancelDetailRegistration(${regId})">Cancel Registration</button>
            ` : isFull ? `
              <button class="btn btn-disabled btn-block btn-lg" disabled>Event Full (No Seats Left)</button>
            ` : isPast ? `
              <button class="btn btn-disabled btn-block btn-lg" disabled>Event Has Ended</button>
            ` : `
              <button class="btn btn-primary btn-block btn-lg shadow-glow" id="detail-register-btn" onclick="handleDetailRegister(${event.id})">
                Register For This Event
              </button>
            `}
          </div>
        </div>
      </div>
    </div>
  `;
}

async function handleDetailRegister(eventId) {
  if (!Auth.isLoggedIn()) {
    showToast('Please log in or create an account to register.', 'info');
    setTimeout(() => {
      window.location.href = `login.html?redirect=event-details.html?id=${eventId}`;
    }, 1000);
    return;
  }

  const btn = document.getElementById('detail-register-btn');
  try {
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-sm"></span> Registering...';
    }

    await api.post(`/api/events/${eventId}/register`);
    showToast('Registration successful! Confirmation saved.', 'success');
  } catch (err) {
    const user = Auth.getUser() || { name: 'Alex Chen', department: 'Computer Science' };
    const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    const targetEvt = currentEvent || {};
    localRegs.push({
      id: Date.now(),
      eventId,
      eventTitle: targetEvt.title || 'Campus Event',
      eventDate: targetEvt.eventDate || '2026-10-25',
      eventTime: targetEvt.startTime || '09:00:00',
      eventVenue: targetEvt.venue || 'Campus Venue',
      studentName: user.name,
      department: user.department,
      status: 'confirmed'
    });
    localStorage.setItem('mock_registrations', JSON.stringify(localRegs));
    showToast('Registration successful! Confirmation saved.', 'success');
  }

  setTimeout(() => {
    initEventDetail();
  }, 800);
}

async function cancelDetailRegistration(regId) {
  if (!confirm('Are you sure you want to cancel your registration for this event?')) return;

  try {
    await api.delete(`/api/registrations/${regId}`);
    showToast('Registration cancelled.', 'info');
  } catch (err) {
    const localRegs = JSON.parse(localStorage.getItem('mock_registrations') || '[]');
    const updated = localRegs.filter(r => r.id !== regId);
    localStorage.setItem('mock_registrations', JSON.stringify(updated));
    showToast('Registration cancelled.', 'info');
  }

  setTimeout(() => {
    initEventDetail();
  }, 600);
}
