import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Event — POJO representing a campus event.
 *
 * OOP concepts:
 *   - Encapsulation (private fields, getters/setters)
 *   - Constructor overloading
 *   - toJson() for API response serialization
 *   - Comparable/Comparator support for sorting algorithms
 */
public class Event {

    private int           id;
    private String        title;
    private String        description;
    private String        category;
    private LocalDate     eventDate;
    private LocalTime     startTime;
    private LocalTime     endTime;
    private String        venue;
    private String        organizer;
    private int           capacity;
    private int           registered;
    private String        imageUrl;
    private String        rules;
    private String        eligibility;
    private String        status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Constructors (overloaded) ────────────────────────────────────────────

    public Event() {}

    public Event(int id, String title, String category, LocalDate eventDate, String status) {
        this.id        = id;
        this.title     = title;
        this.category  = category;
        this.eventDate = eventDate;
        this.status    = status;
    }

    // ─── Business logic helpers ───────────────────────────────────────────────

    public int getAvailableSeats() {
        return Math.max(0, capacity - registered);
    }

    public boolean isOpen() {
        return "open".equalsIgnoreCase(status);
    }

    public boolean isFull() {
        return "full".equalsIgnoreCase(status) || registered >= capacity;
    }

    public boolean isActive() {
        return "open".equalsIgnoreCase(status) || "upcoming".equalsIgnoreCase(status);
    }

    // ─── JSON Serialization ───────────────────────────────────────────────────

    public String toJson() {
        return "{" +
               "\"id\":"           + id                                              + "," +
               "\"title\":"        + JsonUtil.quote(title)                           + "," +
               "\"description\":"  + JsonUtil.quote(description)                    + "," +
               "\"category\":"     + JsonUtil.quote(category)                       + "," +
               "\"eventDate\":"    + JsonUtil.quote(eventDate  != null ? eventDate.toString()  : "") + "," +
               "\"startTime\":"    + JsonUtil.quote(startTime  != null ? startTime.toString()  : "") + "," +
               "\"endTime\":"      + JsonUtil.quote(endTime    != null ? endTime.toString()    : "") + "," +
               "\"venue\":"        + JsonUtil.quote(venue)                           + "," +
               "\"organizer\":"    + JsonUtil.quote(organizer)                      + "," +
               "\"capacity\":"     + capacity                                        + "," +
               "\"registered\":"   + registered                                      + "," +
               "\"available\":"    + getAvailableSeats()                            + "," +
               "\"imageUrl\":"     + JsonUtil.quote(imageUrl)                       + "," +
               "\"rules\":"        + JsonUtil.quote(rules)                          + "," +
               "\"eligibility\":"  + JsonUtil.quote(eligibility)                    + "," +
               "\"status\":"       + JsonUtil.quote(status)                         + "," +
               "\"createdAt\":"    + JsonUtil.quote(createdAt  != null ? createdAt.toString()  : "") + "," +
               "\"updatedAt\":"    + JsonUtil.quote(updatedAt  != null ? updatedAt.toString()  : "") +
               "}";
    }

    /** Summary JSON for listing pages (lighter payload) */
    public String toSummaryJson() {
        return "{" +
               "\"id\":"          + id                                              + "," +
               "\"title\":"       + JsonUtil.quote(title)                          + "," +
               "\"category\":"    + JsonUtil.quote(category)                       + "," +
               "\"eventDate\":"   + JsonUtil.quote(eventDate != null ? eventDate.toString() : "") + "," +
               "\"startTime\":"   + JsonUtil.quote(startTime != null ? startTime.toString() : "") + "," +
               "\"venue\":"       + JsonUtil.quote(venue)                          + "," +
               "\"organizer\":"   + JsonUtil.quote(organizer)                      + "," +
               "\"capacity\":"    + capacity                                        + "," +
               "\"registered\":"  + registered                                      + "," +
               "\"available\":"   + getAvailableSeats()                            + "," +
               "\"imageUrl\":"    + JsonUtil.quote(imageUrl)                       + "," +
               "\"status\":"      + JsonUtil.quote(status)                         +
               "}";
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", title=" + title + ", date=" + eventDate + ", status=" + status + "}";
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                         { return id; }
    public void setId(int id)                  { this.id = id; }

    public String getTitle()                   { return title; }
    public void setTitle(String t)             { this.title = t; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public String getCategory()                { return category; }
    public void setCategory(String c)          { this.category = c; }

    public LocalDate getEventDate()            { return eventDate; }
    public void setEventDate(LocalDate d)      { this.eventDate = d; }

    public LocalTime getStartTime()            { return startTime; }
    public void setStartTime(LocalTime t)      { this.startTime = t; }

    public LocalTime getEndTime()              { return endTime; }
    public void setEndTime(LocalTime t)        { this.endTime = t; }

    public String getVenue()                   { return venue; }
    public void setVenue(String v)             { this.venue = v; }

    public String getOrganizer()               { return organizer; }
    public void setOrganizer(String o)         { this.organizer = o; }

    public int getCapacity()                   { return capacity; }
    public void setCapacity(int c)             { this.capacity = c; }

    public int getRegistered()                 { return registered; }
    public void setRegistered(int r)           { this.registered = r; }

    public String getImageUrl()                { return imageUrl; }
    public void setImageUrl(String u)          { this.imageUrl = u; }

    public String getRules()                   { return rules; }
    public void setRules(String r)             { this.rules = r; }

    public String getEligibility()             { return eligibility; }
    public void setEligibility(String e)       { this.eligibility = e; }

    public String getStatus()                  { return status; }
    public void setStatus(String s)            { this.status = s; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime d)  { this.createdAt = d; }

    public LocalDateTime getUpdatedAt()        { return updatedAt; }
    public void setUpdatedAt(LocalDateTime d)  { this.updatedAt = d; }
}
