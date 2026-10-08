import java.sql.SQLException;
import java.util.*;

/**
 * AdminService — Admin dashboard statistics and user management.
 *
 * Algorithms:
 *   - Aggregation counting (total events, users, registrations)
 *   - LinkedHashMap to preserve ordering in stat maps
 *   - Department-wise registration breakdown
 */
public class AdminService {

    private final UserDAO         userDAO         = new UserDAO();
    private final EventDAO        eventDAO        = new EventDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final EventService    eventService    = new EventService();
    private final RegistrationService regService  = new RegistrationService();

    // ─── Dashboard Statistics ─────────────────────────────────────────────────

    /**
     * Returns a comprehensive statistics map for the admin dashboard.
     * Uses LinkedHashMap to preserve display order.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        try {
            stats.put("totalUsers",         userDAO.countStudents());
            stats.put("totalEvents",         eventDAO.countTotal());
            stats.put("totalRegistrations",  registrationDAO.countTotal());
            stats.put("upcomingEvents",      eventDAO.countUpcoming());

            // Top 5 most popular events
            List<Event> topEvents = eventDAO.getMostPopular(5);
            List<String> topEventJsons = new ArrayList<>();
            for (Event e : topEvents) topEventJsons.add(e.toSummaryJson());
            stats.put("topEventsJson", JsonUtil.listToJsonArray(topEventJsons));

            // Recent 10 registrations
            List<Registration> recent = registrationDAO.getRecent(10);
            List<String> recentJsons = new ArrayList<>();
            for (Registration r : recent) recentJsons.add(r.toAdminJson());
            stats.put("recentRegistrationsJson", JsonUtil.listToJsonArray(recentJsons));

            // Department-wise breakdown
            Map<String, Integer> deptMap = userDAO.getRegistrationsByDepartment();
            stats.put("departmentStatsJson", deptMapToJson(deptMap));

        } catch (SQLException e) {
            System.err.println("[Admin] Stats error: " + e.getMessage());
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ─── User Management ─────────────────────────────────────────────────────

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllStudents();
    }

    public boolean deleteUser(int userId) throws SQLException {
        return userDAO.deleteUser(userId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String deptMapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("{\"department\":").append(JsonUtil.quote(e.getKey()))
              .append(",\"count\":").append(e.getValue()).append("}");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
