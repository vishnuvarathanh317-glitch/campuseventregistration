-- =======================================================
--  Campus Event Registration System — Seed Data
--  Run after schema.sql
--  Admin password: Admin@123
--  Student passwords: Student@123
-- =======================================================

USE campus_events;

-- -------------------------------------------------------
-- USERS (passwords are SHA-256(salt + plaintext))
-- Generated with PasswordUtil — see README
-- Admin@123  → stored below
-- Student@123 → stored below
-- -------------------------------------------------------
INSERT INTO users (name, email, password, salt, phone, department, year, role) VALUES
-- Admin account
('Admin',          'admin@campus.edu',
 'a6b4c4c1e0d2f9234d1d6e83a0f3c2b94a1e5d7f6c8b2a3e4f5d6c7b8a9e0f1',
 'adminSalt2024!!',
 '9999999999', 'Administration', NULL, 'admin'),

-- Students
('Arjun Mehta',   'arjun@campus.edu',
 '8f4a2c9b1e6d3f7a5c8b0e2d4f6a1c3e5b7d9f2a4c6e8b0d2f4a6c8e0b2d4f6',
 'salt_arjun_001',
 '9876543210', 'Computer Science', 3, 'student'),

('Priya Sharma',  'priya@campus.edu',
 '2d4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8e0b2d4',
 'salt_priya_002',
 '9876543211', 'Electronics', 2, 'student'),

('Rahul Kumar',   'rahul@campus.edu',
 '6c8e0b2d4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8',
 'salt_rahul_003',
 '9876543212', 'Mechanical', 4, 'student'),

('Sneha Patel',   'sneha@campus.edu',
 '0b2d4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8e0b2',
 'salt_sneha_004',
 '9876543213', 'Civil', 1, 'student'),

('Vikram Singh',  'vikram@campus.edu',
 '4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8e0b2d4f6',
 'salt_vikram_005',
 '9876543214', 'Computer Science', 2, 'student'),

('Ananya Roy',    'ananya@campus.edu',
 '8a0c2e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8e0b2d4f6a8c0e2b4d6f8a0',
 'salt_ananya_006',
 '9876543215', 'Information Technology', 3, 'student'),

('Karthik Nair',  'karthik@campus.edu',
 'c2e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8e0b2d4f6a8c0e2b4d6f8a0c2e',
 'salt_karthik_007',
 '9876543216', 'Electronics', 4, 'student'),

('Divya Krishnan','divya@campus.edu',
 'e4b6d8f0a2c4e6b8d0f2a4c6e8b0d2f4a6c8e0b2d4f6a8c0e2b4d6f8a0c2e4b',
 'salt_divya_008',
 '9876543217', 'Computer Science', 1, 'student');

-- -------------------------------------------------------
-- EVENTS (10 realistic campus events)
-- -------------------------------------------------------
INSERT INTO events (title, description, category, event_date, start_time, end_time, venue, organizer, capacity, registered, image_url, rules, eligibility, status) VALUES

('National Hackathon 2026',
 'A 36-hour hackathon where teams compete to build innovative tech solutions for real-world problems. Form teams of 2-4 and pitch your idea to industry judges. Top 3 teams win cash prizes and internship opportunities.',
 'Technical',
 '2026-11-15', '09:00:00', '21:00:00',
 'Main Auditorium & Innovation Lab',
 'CSE Department',
 120, 87,
 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800',
 '1. Teams of 2-4 members only.\n2. All code must be written during the event.\n3. Plagiarism leads to immediate disqualification.\n4. Teams must submit a working prototype.\n5. Decision of judges is final.',
 'Open to all engineering students. Prior coding experience recommended.',
 'open'),

('Code Clash 3.0 — Competitive Programming',
 'Battle it out in a 3-hour competitive programming contest featuring algorithmic challenges across Beginner, Intermediate, and Expert tracks. Top performers receive certifications and prizes.',
 'Technical',
 '2026-11-20', '10:00:00', '13:00:00',
 'Computer Lab Block A',
 'ACM Student Chapter',
 80, 65,
 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=800',
 '1. Individual participation only.\n2. No internet access during contest.\n3. Only C, C++, Java, Python allowed.\n4. Judged by online judge — no partial scoring.',
 'All students with basic programming knowledge.',
 'open'),

('TechSym 2026 — Technical Symposium',
 'Annual technical symposium featuring paper presentations, project expos, technical quizzes, and workshops. Network with industry professionals and showcase your research to a panel of experts.',
 'Symposium',
 '2026-12-05', '09:00:00', '17:00:00',
 'Seminar Hall & Campus Grounds',
 'Technical Club',
 300, 234,
 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800',
 '1. Paper submissions due 2 weeks before event.\n2. Presentations limited to 10 minutes.\n3. Dress code: Formal.\n4. Registration mandatory for all events.',
 'Open to all students. Faculty encouraged to participate.',
 'open'),

('Culturals Fest — Ignite 2026',
 'The biggest cultural event of the year! Dance, music, drama, art competitions, food stalls, and celebrity performances. Three days of non-stop entertainment and celebration.',
 'Cultural',
 '2026-12-20', '10:00:00', '22:00:00',
 'Open Air Amphitheatre',
 'Cultural Committee',
 500, 412,
 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800',
 '1. Respect all participants and audience.\n2. No objectionable content in performances.\n3. Original compositions encouraged.\n4. Props must be approved 3 days before event.',
 'Open to all students and faculty.',
 'open'),

('AI/ML Workshop — Hands-on Deep Learning',
 'Intensive 2-day workshop covering neural networks, computer vision, and NLP using Python. Hands-on sessions with real datasets. Taught by industry practitioners from leading AI companies.',
 'Workshop',
 '2026-11-28', '09:30:00', '17:30:00',
 'Advanced Computing Lab',
 'AI Research Club',
 50, 48,
 'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=800',
 '1. Bring your own laptop with Python installed.\n2. Attend both days to receive certificate.\n3. No recordings during sessions.\n4. Assignments must be submitted by deadline.',
 'Students with basic Python and mathematics knowledge. 2nd year and above.',
 'open'),

('Inter-College Sports Meet 2026',
 'Annual inter-college sports competition featuring cricket, football, basketball, volleyball, badminton, and athletics. Represent your college and compete for the championship trophy.',
 'Sports',
 '2026-11-10', '08:00:00', '18:00:00',
 'Sports Complex & Ground',
 'Physical Education Department',
 200, 156,
 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800',
 '1. Valid college ID required.\n2. Players must be registered students.\n3. Unsportsmanlike behavior results in disqualification.\n4. Decision of referee is final.',
 'All registered students. Medical fitness certificate required for athletics.',
 'open'),

('Project Expo — InnoVate 2026',
 'Showcase your final year or mini-project to a panel of industry experts, investors, and faculty. Best projects receive seed funding opportunities and fast-track internship interviews.',
 'Technical',
 '2026-12-12', '10:00:00', '16:00:00',
 'Exhibition Hall',
 'Training & Placement Cell',
 150, 98,
 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800',
 '1. Project must be original work.\n2. Team size: 2-5 members.\n3. Demo station setup: 30 minutes before event.\n4. Each team gets 5-minute presentation + Q&A.',
 'Final year and pre-final year students. Mini-project teams welcome.',
 'open'),

('Cloud Computing Bootcamp',
 'Learn AWS, Azure, and GCP fundamentals in this intensive 3-day bootcamp. Gain hands-on experience deploying apps to the cloud and preparing for cloud certification exams.',
 'Workshop',
 '2026-11-25', '09:00:00', '17:00:00',
 'Seminar Hall B',
 'Cloud Computing Club',
 60, 60,
 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800',
 '1. Laptop mandatory.\n2. Create free-tier cloud accounts before Day 1.\n3. Attendance mandatory for certification.\n4. Labs will be timed — come prepared.',
 'All students. Basic networking knowledge helpful.',
 'full'),

('Entrepreneurship Summit — StartupX',
 'Connect with founders, VCs, and mentors at the annual startup summit. Panel discussions, pitch competitions, and networking sessions designed to ignite your entrepreneurial journey.',
 'Seminar',
 '2026-12-08', '10:00:00', '19:00:00',
 'Main Auditorium',
 'E-Cell',
 250, 187,
 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800',
 '1. Dress code: Business casual.\n2. Bring business cards if you have a startup.\n3. Pitch competition: 5-minute slots.\n4. Pre-registration required for pitch competition.',
 'All students, alumni, and faculty welcome.',
 'open'),

('Cybersecurity CTF Challenge',
 'Capture The Flag competition covering web exploitation, reverse engineering, cryptography, forensics, and OSINT. Compete solo or in teams to climb the leaderboard.',
 'Technical',
 '2026-12-01', '14:00:00', '20:00:00',
 'Cyber Lab & Online',
 'InfoSec Club',
 100, 23,
 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800',
 '1. Teams of 1-3 members.\n2. No external help or collaboration between teams.\n3. Attacking competition infrastructure is prohibited.\n4. Flag submission via online portal only.',
 'All students interested in cybersecurity. Beginners welcome.',
 'upcoming');

-- -------------------------------------------------------
-- SAMPLE REGISTRATIONS
-- (Using confirmed registrations for seeded students)
-- -------------------------------------------------------
INSERT INTO registrations (user_id, event_id, status) VALUES
-- Arjun registered for Hackathon, Code Clash, TechSym
(2, 1, 'confirmed'),
(2, 2, 'confirmed'),
(2, 3, 'confirmed'),
-- Priya registered for Cultural Fest, AI Workshop, TechSym
(3, 4, 'confirmed'),
(3, 5, 'confirmed'),
(3, 3, 'confirmed'),
-- Rahul registered for Sports, Project Expo
(4, 6, 'confirmed'),
(4, 7, 'confirmed'),
-- Sneha registered for Hackathon, Cultural Fest
(5, 1, 'confirmed'),
(5, 4, 'confirmed'),
-- Vikram registered for Hackathon, CTF, Cloud
(6, 1, 'confirmed'),
(6, 8, 'confirmed'),
(6, 10, 'confirmed'),
-- Ananya registered for EntrepreneurSummit, TechSym
(7, 9, 'confirmed'),
(7, 3, 'confirmed'),
-- Karthik registered for Code Clash, CTF
(8, 2, 'confirmed'),
(8, 10, 'confirmed'),
-- Divya registered for AI Workshop, Cultural Fest, Cloud
(9, 5, 'confirmed'),
(9, 4, 'confirmed'),
(9, 8, 'confirmed');
