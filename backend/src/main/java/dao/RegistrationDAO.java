import java.sql.*;
import java.util.*;

/**
 * RegistrationDAO — Database Access Object for registrations table.
 *
 * Key: duplicate prevention enforced at both DB level (UNIQUE constraint)
 * and application level (pre-check before INSERT).
 */
public class RegistrationDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    /**
     * Creates a new registration.
     * @throws SQLException if DB constraint fails (e.g. duplicate)
     * @return generated registration ID, or -1 on failure
     */
    public int create(int userId, int eventId) throws SQLException {
        String sql = "INSERT INTO registrations (user_id, event_id, status) VALUES (?, ?, 'confirmed')";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ─── Duplicate Check ──────────────────────────────────────────────────────

    /**
     * Checks if a confirmed registration already exists.
     * Called before INSERT to give a clear error message.
     */
    public boolean exists(int userId, int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE user_id=? AND event_id=? AND status='confirmed'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public Registration findById(int id) throws SQLException {
        String sql = "SELECT * FROM registrations WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Get all registrations for a student (with event info) */
    public List<Registration> findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT r.*, e.title AS event_title, e.event_date, e.category
            FROM registrations r
            JOIN events e ON r.event_id = e.id
            WHERE r.user_id = ?
            ORDER BY r.registration_date DESC
            """;
        List<Registration> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Registration reg = mapRow(rs);
                    reg.setEventTitle(rs.getString("event_title"));
                    reg.setEventDate(rs.getString("event_date"));
                    reg.setEventCategory(rs.getString("category"));
                    list.add(reg);
                }
            }
        }
        return list;
    }

    /**
     * Get all registrations with full user + event info (admin view).
     * Supports optional search, event filter, department filter, year filter.
     *
     * Algorithm: DB-level filtering for performance, then Java-level search.
     */
    public List<Registration> findAll(String search, Integer eventId,
                                      String department, Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT r.*, u.name, u.email, u.department, u.year, u.phone,
                   e.title AS event_title, e.event_date, e.category
            FROM registrations r
            JOIN users u  ON r.user_id  = u.id
            JOIN events e ON r.event_id = e.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (eventId != null && eventId > 0) {
            sql.append(" AND r.event_id = ?");
            params.add(eventId);
        }
        if (department != null && !department.isBlank()) {
            sql.append(" AND u.department = ?");
            params.add(department);
        }
        if (year != null && year > 0) {
            sql.append(" AND u.year = ?");
            params.add(year);
        }

        sql.append(" ORDER BY r.registration_date DESC");

        List<Registration> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Registration reg = mapRow(rs);
                    reg.setStudentName(rs.getString("name"));
                    reg.setStudentEmail(rs.getString("email"));
                    reg.setDepartment(rs.getString("department"));
                    Object y = rs.getObject("year");
                    if (y instanceof Number n) {
                        reg.setYear(n.intValue());
                    } else {
                        reg.setYear(null);
                    }
                    reg.setPhone(rs.getString("phone"));
                    reg.setEventTitle(rs.getString("event_title"));
                    reg.setEventDate(rs.getString("event_date"));
                    reg.setEventCategory(rs.getString("category"));
                    list.add(reg);
                }
            }
        }

        // Java-level search across name/email/event title
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            list.removeIf(r ->
                !containsQuery(r.getStudentName(), q) &&
                !containsQuery(r.getStudentEmail(), q) &&
                !containsQuery(r.getEventTitle(), q) &&
                !containsQuery(r.getDepartment(), q));
        }

        return list;
    }

    private boolean containsQuery(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    public boolean cancel(int registrationId, int userId) throws SQLException {
        // userId guard prevents students from cancelling others' registrations
        String sql = "UPDATE registrations SET status='cancelled' WHERE id=? AND user_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean adminCancel(int registrationId) throws SQLException {
        String sql = "UPDATE registrations SET status='cancelled' WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE status='confirmed'";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countByEvent(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id=? AND status='confirmed'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Recent N registrations for dashboard activity feed */
    public List<Registration> getRecent(int limit) throws SQLException {
        String sql = """
            SELECT r.*, u.name, u.email, e.title AS event_title, e.event_date, e.category
            FROM registrations r
            JOIN users  u ON r.user_id  = u.id
            JOIN events e ON r.event_id = e.id
            ORDER BY r.registration_date DESC
            LIMIT ?
            """;
        List<Registration> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Registration reg = mapRow(rs);
                    reg.setStudentName(rs.getString("name"));
                    reg.setStudentEmail(rs.getString("email"));
                    reg.setEventTitle(rs.getString("event_title"));
                    reg.setEventDate(rs.getString("event_date"));
                    reg.setEventCategory(rs.getString("category"));
                    list.add(reg);
                }
            }
        }
        return list;
    }

    // ─── Row Mapper ───────────────────────────────────────────────────────────

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setEventId(rs.getInt("event_id"));
        r.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("registration_date");
        if (ts != null) r.setRegistrationDate(ts.toLocalDateTime());

        return r;
    }
}
