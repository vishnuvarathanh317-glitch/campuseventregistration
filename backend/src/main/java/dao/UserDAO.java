import java.sql.*;

/**
 * UserDAO — Database Access Object for users table.
 * All queries use PreparedStatement (no SQL injection possible).
 *
 * OOP: Demonstrates encapsulation of all DB operations for User.
 */
public class UserDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    /**
     * Inserts a new user and returns the generated ID.
     * @return generated user ID, or -1 on failure
     */
    public int create(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, salt, phone, department, year, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getPhone());

            if (user instanceof Student s) {
                ps.setString(6, s.getDepartment());
                ps.setInt(7, s.getYear());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.INTEGER);
            }

            ps.setString(8, user.getRole());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    /** Find user by email (for login) */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Find user by ID */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Check if email already exists */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Get all students (admin view) */
    public java.util.List<User> getAllStudents() throws SQLException {
        java.util.List<User> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'student' ORDER BY name";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Get total student count */
    public int countStudents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'student'";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public boolean updatePassword(int userId, String newHash, String newSalt) throws SQLException {
        String sql = "UPDATE users SET password = ?, salt = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ? AND role != 'admin'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Department Stats (for admin dashboard) ────────────────────────────────

    public java.util.Map<String, Integer> getRegistrationsByDepartment() throws SQLException {
        java.util.LinkedHashMap<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = """
            SELECT u.department, COUNT(r.id) AS cnt
            FROM registrations r
            JOIN users u ON r.user_id = u.id
            WHERE r.status = 'confirmed' AND u.department IS NOT NULL
            GROUP BY u.department
            ORDER BY cnt DESC
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("department"), rs.getInt("cnt"));
            }
        }
        return map;
    }

    // ─── Row Mapper ───────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;

        if ("admin".equalsIgnoreCase(role)) {
            Admin admin = new Admin();
            user = admin;
        } else {
            Student student = new Student();
            student.setDepartment(rs.getString("department"));
            Object yearObj = rs.getObject("year");
            if (yearObj instanceof Number n) {
                student.setYear(n.intValue());
            } else {
                student.setYear(0);
            }
            user = student;
        }

        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setSalt(rs.getString("salt"));
        user.setPhone(rs.getString("phone"));
        user.setRole(role);

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());

        return user;
    }
}
