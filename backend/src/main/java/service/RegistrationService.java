import java.sql.SQLException;
import java.util.*;

/**
 * RegistrationService — Implements the 7-step registration validation chain.
 *
 * Validation pipeline (in order):
 *   1. User exists
 *   2. Event exists
 *   3. Event is active (not cancelled/completed)
 *   4. Duplicate registration check
 *   5. Capacity check
 *   6. Register (INSERT)
 *   7. Return confirmation
 *
 * This demonstrates: sequential validation, algorithm design, and service layer patterns.
 */
public class RegistrationService {

    private final UserDAO         userDAO         = new UserDAO();
    private final EventDAO        eventDAO        = new EventDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    // ─── Register for Event ───────────────────────────────────────────────────

    /**
     * Full 7-step registration validation and creation.
     * @return result map with "registrationId" on success or "error" on failure
     */
    public Map<String, Object> register(int userId, int eventId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // STEP 1: User exists?
            User user = userDAO.findById(userId);
            if (user == null) {
                result.put("error", "User account not found.");
                return result;
            }

            // STEP 2: Event exists?
            Event event = eventDAO.findById(eventId);
            if (event == null) {
                result.put("error", "Event not found.");
                return result;
            }

            // STEP 3: Event is registerable?
            if (!event.isActive()) {
                result.put("error", "Registration is closed for this event. Status: " + event.getStatus());
                return result;
            }

            // STEP 4: Duplicate check?
            if (registrationDAO.exists(userId, eventId)) {
                result.put("error", "You are already registered for this event.");
                result.put("duplicate", true);
                return result;
            }

            // STEP 5: Capacity check?
            if (event.isFull()) {
                result.put("error", "This event is full. No seats available.");
                result.put("full", true);
                return result;
            }

            // STEP 6: Create registration
            int registrationId = registrationDAO.create(userId, eventId);
            if (registrationId < 0) {
                result.put("error", "Failed to complete registration. Please try again.");
                return result;
            }

            // STEP 7: Return confirmation
            result.put("registrationId", registrationId);
            result.put("eventTitle",     event.getTitle());
            result.put("eventDate",      event.getEventDate() != null ? event.getEventDate().toString() : "");
            result.put("studentName",    user.getName());
            result.put("success",        true);

        } catch (SQLException e) {
            // Catch UNIQUE constraint violation (concurrent duplicate)
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                result.put("error", "You are already registered for this event.");
                result.put("duplicate", true);
            } else {
                System.err.println("[Registration] Error: " + e.getMessage());
                result.put("error", "Database error during registration.");
            }
        }

        return result;
    }

    // ─── Cancel Registration ──────────────────────────────────────────────────

    public Map<String, Object> cancelRegistration(int registrationId, int userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean cancelled = registrationDAO.cancel(registrationId, userId);
            if (!cancelled) {
                result.put("error", "Registration not found or not authorized to cancel.");
            } else {
                result.put("success", true);
            }
        } catch (SQLException e) {
            result.put("error", "Database error: " + e.getMessage());
        }
        return result;
    }

    // ─── My Registrations ─────────────────────────────────────────────────────

    public List<Registration> getMyRegistrations(int userId) throws SQLException {
        return registrationDAO.findByUserId(userId);
    }

    // ─── Admin: All Registrations ─────────────────────────────────────────────

    public List<Registration> getAllRegistrations(String search, Integer eventId,
                                                  String department, Integer year) throws SQLException {
        return registrationDAO.findAll(search, eventId, department, year);
    }

    // ─── Admin: Cancel ────────────────────────────────────────────────────────

    public Map<String, Object> adminCancelRegistration(int registrationId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean cancelled = registrationDAO.adminCancel(registrationId);
            if (!cancelled) result.put("error", "Registration not found.");
            else            result.put("success", true);
        } catch (SQLException e) {
            result.put("error", "Database error: " + e.getMessage());
        }
        return result;
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    public int getTotalRegistrations()       throws SQLException { return registrationDAO.countTotal(); }
    public int getRegistrationsForEvent(int id) throws SQLException { return registrationDAO.countByEvent(id); }
    public List<Registration> getRecent(int n) throws SQLException { return registrationDAO.getRecent(n); }
}
