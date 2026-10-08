import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * SessionManager — In-memory session store using HashMap.
 *
 * Data structure: HashMap<token, SessionData>
 *   - O(1) lookup per request
 *   - Sessions expire after configurable duration
 *   - Thread-safe via synchronized methods
 */
public class SessionManager {

    private static SessionManager instance;

    // Maps token → SessionData
    private final Map<String, SessionData> sessions = new HashMap<>();

    private static final long SESSION_DURATION_MS = 60 * 60 * 1000L; // 1 hour

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Creates a new session for a user.
     * @param userId the authenticated user's ID
     * @param role   "student" or "admin"
     * @return UUID token to send to client
     */
    public synchronized String createSession(int userId, String role) {
        // Remove any existing sessions for this user
        sessions.entrySet().removeIf(e -> e.getValue().userId == userId);

        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(userId, role, Instant.now().toEpochMilli()));
        return token;
    }

    /**
     * Validates a token and returns its SessionData, or null if invalid/expired.
     */
    public synchronized SessionData getSession(String token) {
        if (token == null || token.isBlank()) return null;
        SessionData data = sessions.get(token);
        if (data == null) return null;

        long now = Instant.now().toEpochMilli();
        if (now - data.createdAt > SESSION_DURATION_MS) {
            sessions.remove(token);
            return null;
        }
        return data;
    }

    /**
     * Invalidates (removes) a session token.
     */
    public synchronized void invalidateSession(String token) {
        sessions.remove(token);
    }

    /**
     * Removes all expired sessions (call periodically).
     */
    public synchronized void cleanExpiredSessions() {
        long now = Instant.now().toEpochMilli();
        Iterator<Map.Entry<String, SessionData>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue().createdAt > SESSION_DURATION_MS) {
                it.remove();
            }
        }
    }

    public int activeSessionCount() {
        return sessions.size();
    }

    // ─── Inner class ────────────────────────────────────────────────────────
    public static class SessionData {
        public final int    userId;
        public final String role;
        public final long   createdAt;

        public SessionData(int userId, String role, long createdAt) {
            this.userId    = userId;
            this.role      = role;
            this.createdAt = createdAt;
        }

        public boolean isAdmin() {
            return "admin".equalsIgnoreCase(role);
        }
    }
}
