import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * EventDAO — Database Access Object for events table.
 *
 * Algorithms implemented:
 *   - Sorting using Comparator (by date, name, available seats, category)
 *   - Linear search across title/description/venue/organizer
 *   - Binary search by ID on sorted list
 *   - Filtering by status, category
 */
public class EventDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    public int create(Event event) throws SQLException {
        String sql = """
            INSERT INTO events
              (title, description, category, event_date, start_time, end_time,
               venue, organizer, capacity, image_url, rules, eligibility, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setDate(4, event.getEventDate() != null ? java.sql.Date.valueOf(event.getEventDate()) : null);
            ps.setTime(5, event.getStartTime() != null ? Time.valueOf(event.getStartTime()) : null);
            ps.setTime(6, event.getEndTime()   != null ? Time.valueOf(event.getEndTime())   : null);
            ps.setString(7,  event.getVenue());
            ps.setString(8,  event.getOrganizer());
            ps.setInt(9,     event.getCapacity());
            ps.setString(10, event.getImageUrl());
            ps.setString(11, event.getRules());
            ps.setString(12, event.getEligibility());
            ps.setString(13, event.getStatus() != null ? event.getStatus() : "upcoming");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public Event findById(int id) throws SQLException {
        String sql = "SELECT * FROM events WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Retrieves all events with optional search, sort, and filter.
     *
     * Algorithm:
     *   1. Fetch all from DB with optional status filter
     *   2. Apply keyword search (linear scan across fields)
     *   3. Sort using Java Comparator
     *
     * @param search   keyword for linear text search (null = no search)
     * @param status   filter by status (null = all)
     * @param category filter by category (null = all)
     * @param sortBy   "date" | "title" | "available" | "category"
     * @param asc      true = ascending, false = descending
     */
    public List<Event> findAll(String search, String status, String category,
                               String sortBy, boolean asc) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM events WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // DB-level filter
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }

        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
        }

        // ── Linear Search across multiple fields ─────────────────────────────
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            events = linearSearch(events, q);
        }

        // ── Sort using Comparator ─────────────────────────────────────────────
        Comparator<Event> comparator = buildComparator(sortBy);
        if (!asc) comparator = comparator.reversed();
        events.sort(comparator);

        return events;
    }

    /**
     * Linear search: scans title, description, venue, organizer, category.
     * Time complexity: O(n * k) where k = average string length.
     */
    private List<Event> linearSearch(List<Event> events, String keyword) {
        List<Event> results = new ArrayList<>();
        for (Event e : events) {
            if (containsKeyword(e.getTitle(),       keyword) ||
                containsKeyword(e.getDescription(), keyword) ||
                containsKeyword(e.getVenue(),        keyword) ||
                containsKeyword(e.getOrganizer(),    keyword) ||
                containsKeyword(e.getCategory(),     keyword)) {
                results.add(e);
            }
        }
        return results;
    }

    private boolean containsKeyword(String field, String keyword) {
        return field != null && field.toLowerCase().contains(keyword);
    }

    /**
     * Binary search by ID (events must be sorted by ID first).
     * Time complexity: O(log n)
     */
    public Event binarySearchById(List<Event> sortedEvents, int targetId) {
        int lo = 0, hi = sortedEvents.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int midId = sortedEvents.get(mid).getId();
            if (midId == targetId)      return sortedEvents.get(mid);
            else if (midId < targetId) lo = mid + 1;
            else                        hi = mid - 1;
        }
        return null;
    }

    /**
     * Builds a Comparator for multi-field event sorting.
     */
    private Comparator<Event> buildComparator(String sortBy) {
        if (sortBy == null) sortBy = "date";
        return switch (sortBy.toLowerCase()) {
            case "title"     -> Comparator.comparing(e -> e.getTitle().toLowerCase());
            case "available" -> Comparator.comparingInt(Event::getAvailableSeats);
            case "category"  -> Comparator.comparing(e -> e.getCategory().toLowerCase());
            default          -> Comparator.comparing(e ->
                    e.getEventDate() != null ? e.getEventDate() : LocalDate.MAX);
        };
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public boolean update(Event event) throws SQLException {
        String sql = """
            UPDATE events SET
              title=?, description=?, category=?, event_date=?, start_time=?, end_time=?,
              venue=?, organizer=?, capacity=?, image_url=?, rules=?, eligibility=?, status=?
            WHERE id=?
            """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setDate(4, event.getEventDate() != null ? java.sql.Date.valueOf(event.getEventDate()) : null);
            ps.setTime(5, event.getStartTime() != null ? Time.valueOf(event.getStartTime()) : null);
            ps.setTime(6, event.getEndTime()   != null ? Time.valueOf(event.getEndTime())   : null);
            ps.setString(7,  event.getVenue());
            ps.setString(8,  event.getOrganizer());
            ps.setInt(9,     event.getCapacity());
            ps.setString(10, event.getImageUrl());
            ps.setString(11, event.getRules());
            ps.setString(12, event.getEligibility());
            ps.setString(13, event.getStatus());
            ps.setInt(14,    event.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int eventId, String status) throws SQLException {
        String sql = "UPDATE events SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    public int countTotal() throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("SELECT COUNT(*) FROM events");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countUpcoming() throws SQLException {
        String sql = "SELECT COUNT(*) FROM events WHERE status IN ('upcoming','open') AND event_date >= CURDATE()";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Returns top N most popular events by registration count */
    public List<Event> getMostPopular(int limit) throws SQLException {
        String sql = "SELECT * FROM events ORDER BY registered DESC LIMIT ?";
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ─── Row Mapper ───────────────────────────────────────────────────────────

    private Event mapRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setCategory(rs.getString("category"));

        java.sql.Date d = rs.getDate("event_date");
        if (d != null) e.setEventDate(d.toLocalDate());

        Time st = rs.getTime("start_time");
        if (st != null) e.setStartTime(st.toLocalTime());

        Time et = rs.getTime("end_time");
        if (et != null) e.setEndTime(et.toLocalTime());

        e.setVenue(rs.getString("venue"));
        e.setOrganizer(rs.getString("organizer"));
        e.setCapacity(rs.getInt("capacity"));
        e.setRegistered(rs.getInt("registered"));
        e.setImageUrl(rs.getString("image_url"));
        e.setRules(rs.getString("rules"));
        e.setEligibility(rs.getString("eligibility"));
        e.setStatus(rs.getString("status"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) e.setCreatedAt(ca.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) e.setUpdatedAt(ua.toLocalDateTime());

        return e;
    }
}
