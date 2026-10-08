# ⚡ Campus Event Registration System — Full Stack Project

A complete, production-grade **Campus Event Registration System** built for universities and colleges.

---

## 🌟 Features Overview

### 🎓 Students / Attendees
* **Account Management**: Register with department and academic year, live password strength meter, secure login/logout.
* **Events Explorer**: Real-time debounce search, multi-category filters (*Technical, Cultural, Sports, Workshop, Seminar*), status filters (*Open, Upcoming, Sold Out, Completed*), and multiple sort options (*Date, Seats, Title*).
* **Live Event Details**: Schedule breakdown, venue info, organizer details, rules, eligibility, and dynamic capacity progress bars.
* **One-Click Registration**: Instant confirmation with duplicate registration prevention and capacity overflow guards.
* **Digital Event Passes**: Dedicated "My Passes" section with digital tickets featuring pass codes, event details, and entry QR codes.
* **Cancellation**: Self-service pass cancellation with automatic seat count restoration.
* **Student Dashboard**: Personalized metric cards, enrolled events timeline, and recommended events.

### 🛡️ Admin Portal
* **Executive Dashboard**: System metrics (*Total Events, Total Registrations, Registered Students, Active Events*), department registration share breakdown, and most popular events.
* **Event Management**: Create, Edit, Delete events with full validation (date, timing, category, venue, organizer, capacity limits, rules, and image banner).
* **Registration Roster**: Search attendees by name/email, filter by event and department, view live statuses, and cancel student registrations.
* **User Management**: View complete student directory, academic year, department, and manage accounts.
* **Data Export Center**: One-click CSV downloads for *Event Registrations*, *Student Accounts*, and *Master Events Directory*.

---

## 🛠️ Technology Stack

### Backend
* **Language**: Java 17+ (Core Java, OOP principles: Abstraction, Inheritance, Encapsulation, Polymorphism)
* **HTTP Server**: Built-in `com.sun.net.httpserver.HttpServer` (zero external web frameworks)
* **Database**: MySQL 8.0+ via JDBC (`DatabaseConnection` connection pool)
* **Security**: Salted SHA-256 password hashing (`PasswordUtil`) & in-memory session token store (`SessionManager`)
* **Data Export**: Custom CSV streaming engine (`CSVExporter`)
* **JSON Processing**: Custom lightweight JSON serializer/parser (`JsonUtil`)

### Frontend
* **Core**: HTML5 Semantic markup & Vanilla JavaScript (ES6+ async/await, Fetch API)
* **Styling**: Vanilla CSS3 with CSS Custom Properties (Design Tokens), Glassmorphism, Responsive Grid/Flexbox layouts, and Keyframe Animations.

---

## 🗄️ Database Setup

1. Make sure **MySQL Server** is running on your local machine.
2. Open MySQL CLI or MySQL Workbench and run:

```sql
SOURCE backend/database/schema.sql;
SOURCE backend/database/seed.sql;
```

Or copy-paste the contents of [`backend/database/schema.sql`](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/campuseventregistration/backend/database/schema.sql) followed by [`backend/database/seed.sql`](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/campuseventregistration/backend/database/seed.sql).

3. Verify or update credentials in [`backend/config.properties`](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/campuseventregistration/backend/config.properties):

```properties
db.host=localhost
db.port=3306
db.name=campus_events
db.username=root
db.password=root
server.port=8080
```

---

## 🚀 How to Run

### Step 1: Compile the Backend
Open a terminal in the `backend` folder and run:

```cmd
compile.bat
```

### Step 2: Start the Java HTTP Server
```cmd
run.bat
```
The server will start listening on `http://localhost:8080`.

### Step 3: Open the Frontend
Open [`frontend/index.html`](file:///c:/Users/vishnuvarathanh/OneDrive/Desktop/campuseventregistration/frontend/index.html) directly in any web browser, or serve using VS Code Live Server / Python HTTP server:

```cmd
cd frontend
python -m http.server 3000
```
Then navigate to `http://localhost:3000`.

---

## 🔑 Demo Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@campus.edu` | `Admin@123` |
| **Student** | `alex.chen@student.campus.edu` | `Student@123` |
| **Student** | `priya.patel@student.campus.edu` | `Student@123` |

*(Note: The login screen also features **"Quick Demo Autofill"** buttons for one-click testing.)*
