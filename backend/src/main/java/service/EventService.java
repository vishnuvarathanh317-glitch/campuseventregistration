import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * EventService — Business logic for event management.
 *
 * Wraps EventDAO with validation and delegates to sorting/search algorithms.
 */
public class EventService {

    private final EventDAO eventDAO = new EventDAO();

    // ─── Get Events (with search, sort, filter) ───────────────────────────────

    public List<Event> getEvents(String search, String status, String category,
                                 String sortBy, boolean asc) throws SQLException {
        return eventDAO.findAll(search, status, category, sortBy, asc);
    }

    // ─── Get Single Event ─────────────────────────────────────────────────────

    public Event getEventById(int id) throws SQLException {
        return eventDAO.findById(id);
    }

    // ─── Create Event ─────────────────────────────────────────────────────────

    public Map<String, Object> createEvent(String body) {
        Map<String, Object> result = new HashMap<>();

        String title    = JsonUtil.extractValue(body, "title");
        String desc     = JsonUtil.extractValue(body, "description");
        String category = JsonUtil.extractValue(body, "category");
        String dateStr  = JsonUtil.extractValue(body, "eventDate");
        String startStr = JsonUtil.extractValue(body, "startTime");
        String endStr   = JsonUtil.extractValue(body, "endTime");
        String venue    = JsonUtil.extractValue(body, "venue");
        String organizer= JsonUtil.extractValue(body, "organizer");
        int    capacity = JsonUtil.extractInt(body, "capacity", 0);
        String imageUrl = JsonUtil.extractValue(body, "imageUrl");
        String rules    = JsonUtil.extractValue(body, "rules");
        String eligibility = JsonUtil.extractValue(body, "eligibility");
        String status   = JsonUtil.extractValue(body, "status");

        // Validate
        if (ValidationUtil.isBlank(title))    { result.put("error", "Event title is required.");    return result; }
        if (ValidationUtil.isBlank(category)) { result.put("error", "Category is required.");        return result; }
        if (ValidationUtil.isBlank(dateStr))  { result.put("error", "Event date is required.");      return result; }
        String capErr = ValidationUtil.validateCapacity(capacity);
        if (capErr != null)                   { result.put("error", capErr);                         return result; }

        try {
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(desc);
            event.setCategory(category);
            event.setEventDate(LocalDate.parse(dateStr));
            if (startStr != null && !startStr.isBlank()) event.setStartTime(LocalTime.parse(startStr));
            if (endStr   != null && !endStr.isBlank())   event.setEndTime(LocalTime.parse(endStr));
            event.setVenue(venue);
            event.setOrganizer(organizer);
            event.setCapacity(capacity);
            event.setImageUrl(imageUrl);
            event.setRules(rules);
            event.setEligibility(eligibility);
            event.setStatus(status != null && !status.isBlank() ? status : "upcoming");

            int id = eventDAO.create(event);
            if (id < 0) { result.put("error", "Failed to create event."); return result; }

            event.setId(id);
            result.put("event", event.toJson());
            result.put("id", id);

        } catch (Exception e) {
            result.put("error", "Error creating event: " + e.getMessage());
        }

        return result;
    }

    // ─── Update Event ─────────────────────────────────────────────────────────

    public Map<String, Object> updateEvent(int eventId, String body) {
        Map<String, Object> result = new HashMap<>();

        try {
            Event existing = eventDAO.findById(eventId);
            if (existing == null) { result.put("error", "Event not found."); return result; }

            // Update fields from body (only if provided)
            String title    = JsonUtil.extractValue(body, "title");
            String desc     = JsonUtil.extractValue(body, "description");
            String category = JsonUtil.extractValue(body, "category");
            String dateStr  = JsonUtil.extractValue(body, "eventDate");
            String startStr = JsonUtil.extractValue(body, "startTime");
            String endStr   = JsonUtil.extractValue(body, "endTime");
            String venue    = JsonUtil.extractValue(body, "venue");
            String organizer= JsonUtil.extractValue(body, "organizer");
            String capStr   = JsonUtil.extractValue(body, "capacity");
            String imageUrl = JsonUtil.extractValue(body, "imageUrl");
            String rules    = JsonUtil.extractValue(body, "rules");
            String eligibility = JsonUtil.extractValue(body, "eligibility");
            String status   = JsonUtil.extractValue(body, "status");

            if (title    != null) existing.setTitle(title);
            if (desc     != null) existing.setDescription(desc);
            if (category != null) existing.setCategory(category);
            if (dateStr  != null && !dateStr.isBlank()) existing.setEventDate(LocalDate.parse(dateStr));
            if (startStr != null && !startStr.isBlank()) existing.setStartTime(LocalTime.parse(startStr));
            if (endStr   != null && !endStr.isBlank())   existing.setEndTime(LocalTime.parse(endStr));
            if (venue    != null) existing.setVenue(venue);
            if (organizer!= null) existing.setOrganizer(organizer);
            if (capStr   != null) existing.setCapacity(Integer.parseInt(capStr.trim()));
            if (imageUrl != null) existing.setImageUrl(imageUrl);
            if (rules    != null) existing.setRules(rules);
            if (eligibility != null) existing.setEligibility(eligibility);
            if (status   != null) existing.setStatus(status);

            boolean updated = eventDAO.update(existing);
            if (!updated) { result.put("error", "Update failed."); return result; }

            result.put("event", existing.toJson());

        } catch (Exception e) {
            result.put("error", "Error updating event: " + e.getMessage());
        }

        return result;
    }

    // ─── Delete Event ─────────────────────────────────────────────────────────

    public Map<String, Object> deleteEvent(int eventId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = eventDAO.delete(eventId);
            if (!deleted) result.put("error", "Event not found or could not be deleted.");
            else          result.put("success", true);
        } catch (SQLException e) {
            result.put("error", "Database error: " + e.getMessage());
        }
        return result;
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    public int getTotalEvents()    throws SQLException { return eventDAO.countTotal(); }
    public int getUpcomingCount()  throws SQLException { return eventDAO.countUpcoming(); }
    public List<Event> getTopEvents(int n) throws SQLException { return eventDAO.getMostPopular(n); }
}
