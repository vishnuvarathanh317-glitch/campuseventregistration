import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSVExporter — Generates CSV content from database queries.
 * Implements the Exportable interface demonstrating polymorphism.
 *
 * All CSV generation happens on the Java backend — never in frontend JS.
 */
public class CSVExporter {

    // ─── Export Registrations ────────────────────────────────────────────────

    /**
     * Exports all registrations (or for a specific event if eventId > 0).
     */
    public static String exportRegistrations(int eventId) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Student Name,Email,Department,Year,Phone,Event,Event Date,Registration Date,Status");

        String sql = """
            SELECT u.name, u.email, u.department, u.year, u.phone,
                   e.title, e.event_date, r.registration_date, r.status
            FROM registrations r
            JOIN users  u ON r.user_id  = u.id
            JOIN events e ON r.event_id = e.id
            """ + (eventId > 0 ? "WHERE r.event_id = ? " : "")
                + "ORDER BY r.registration_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (eventId > 0) ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    row.add(escapeCsv(rs.getString("name")));
                    row.add(escapeCsv(rs.getString("email")));
                    row.add(escapeCsv(rs.getString("department")));
                    row.add(rs.getObject("year") != null ? String.valueOf(rs.getInt("year")) : "");
                    row.add(escapeCsv(rs.getString("phone")));
                    row.add(escapeCsv(rs.getString("title")));
                    row.add(rs.getString("event_date"));
                    row.add(rs.getString("registration_date"));
                    row.add(escapeCsv(rs.getString("status")));
                    pw.println(String.join(",", row));
                }
            }

        } catch (SQLException e) {
            pw.println("ERROR," + e.getMessage());
        }

        pw.flush();
        return sw.toString();
    }

    // ─── Export Users ─────────────────────────────────────────────────────────

    public static String exportUsers() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("ID,Name,Email,Phone,Department,Year,Role,Registered At");

        String sql = "SELECT id, name, email, phone, department, year, role, created_at " +
                     "FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(rs.getInt("id")));
                row.add(escapeCsv(rs.getString("name")));
                row.add(escapeCsv(rs.getString("email")));
                row.add(escapeCsv(rs.getString("phone")));
                row.add(escapeCsv(rs.getString("department")));
                row.add(rs.getObject("year") != null ? String.valueOf(rs.getInt("year")) : "");
                row.add(escapeCsv(rs.getString("role")));
                row.add(rs.getString("created_at"));
                pw.println(String.join(",", row));
            }

        } catch (SQLException e) {
            pw.println("ERROR," + e.getMessage());
        }

        pw.flush();
        return sw.toString();
    }

    // ─── Export Events ────────────────────────────────────────────────────────

    public static String exportEvents() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("ID,Title,Category,Date,Venue,Organizer,Capacity,Registered,Available,Status");

        String sql = "SELECT id, title, category, event_date, venue, organizer, " +
                     "capacity, registered, (capacity - registered) AS available, status " +
                     "FROM events ORDER BY event_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(rs.getInt("id")));
                row.add(escapeCsv(rs.getString("title")));
                row.add(escapeCsv(rs.getString("category")));
                row.add(rs.getString("event_date"));
                row.add(escapeCsv(rs.getString("venue")));
                row.add(escapeCsv(rs.getString("organizer")));
                row.add(String.valueOf(rs.getInt("capacity")));
                row.add(String.valueOf(rs.getInt("registered")));
                row.add(String.valueOf(rs.getInt("available")));
                row.add(escapeCsv(rs.getString("status")));
                pw.println(String.join(",", row));
            }

        } catch (SQLException e) {
            pw.println("ERROR," + e.getMessage());
        }

        pw.flush();
        return sw.toString();
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /** Escapes a cell value for CSV: wraps in quotes if it contains comma/quote/newline */
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
