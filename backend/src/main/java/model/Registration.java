import java.time.LocalDateTime;

/**
 * Registration — POJO representing a student's registration for an event.
 *
 * OOP concepts:
 *   - Encapsulation
 *   - toJson() serialization
 *   - Enriched view with joined user/event data for admin display
 */
public class Registration {

    private int           id;
    private int           userId;
    private int           eventId;
    private LocalDateTime registrationDate;
    private String        status;

    // ─── Enriched fields (from JOIN queries) ─────────────────────────────────
    private String        studentName;
    private String        studentEmail;
    private String        department;
    private Integer       year;
    private String        phone;
    private String        eventTitle;
    private String        eventDate;
    private String        eventCategory;

    // ─── Constructors (overloaded) ────────────────────────────────────────────

    public Registration() {}

    public Registration(int userId, int eventId) {
        this.userId  = userId;
        this.eventId = eventId;
        this.status  = "confirmed";
    }

    public Registration(int id, int userId, int eventId, LocalDateTime date, String status) {
        this.id               = id;
        this.userId           = userId;
        this.eventId          = eventId;
        this.registrationDate = date;
        this.status           = status;
    }

    // ─── JSON ─────────────────────────────────────────────────────────────────

    /** Basic registration JSON */
    public String toJson() {
        return "{" +
               "\"id\":"               + id                                                   + "," +
               "\"userId\":"           + userId                                               + "," +
               "\"eventId\":"          + eventId                                              + "," +
               "\"registrationDate\":" + JsonUtil.quote(registrationDate != null ? registrationDate.toString() : "") + "," +
               "\"status\":"           + JsonUtil.quote(status)                               +
               "}";
    }

    /** Enriched JSON for admin view (includes student and event info) */
    public String toAdminJson() {
        return "{" +
               "\"id\":"               + id                                                   + "," +
               "\"userId\":"           + userId                                               + "," +
               "\"eventId\":"          + eventId                                              + "," +
               "\"studentName\":"      + JsonUtil.quote(studentName)                         + "," +
               "\"studentEmail\":"     + JsonUtil.quote(studentEmail)                        + "," +
               "\"department\":"       + JsonUtil.quote(department)                          + "," +
               "\"year\":"             + (year != null ? year : "null")                      + "," +
               "\"phone\":"            + JsonUtil.quote(phone)                               + "," +
               "\"eventTitle\":"       + JsonUtil.quote(eventTitle)                          + "," +
               "\"eventDate\":"        + JsonUtil.quote(eventDate)                           + "," +
               "\"eventCategory\":"    + JsonUtil.quote(eventCategory)                       + "," +
               "\"registrationDate\":" + JsonUtil.quote(registrationDate != null ? registrationDate.toString() : "") + "," +
               "\"status\":"           + JsonUtil.quote(status)                              +
               "}";
    }

    /** Student view (includes event info) */
    public String toStudentJson() {
        return "{" +
               "\"id\":"               + id                                                   + "," +
               "\"eventId\":"          + eventId                                              + "," +
               "\"eventTitle\":"       + JsonUtil.quote(eventTitle)                          + "," +
               "\"eventDate\":"        + JsonUtil.quote(eventDate)                           + "," +
               "\"eventCategory\":"    + JsonUtil.quote(eventCategory)                       + "," +
               "\"registrationDate\":" + JsonUtil.quote(registrationDate != null ? registrationDate.toString() : "") + "," +
               "\"status\":"           + JsonUtil.quote(status)                              +
               "}";
    }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", userId=" + userId + ", eventId=" + eventId + ", status=" + status + "}";
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }

    public int getUserId()                              { return userId; }
    public void setUserId(int uid)                      { this.userId = uid; }

    public int getEventId()                             { return eventId; }
    public void setEventId(int eid)                     { this.eventId = eid; }

    public LocalDateTime getRegistrationDate()          { return registrationDate; }
    public void setRegistrationDate(LocalDateTime d)    { this.registrationDate = d; }

    public String getStatus()                           { return status; }
    public void setStatus(String s)                     { this.status = s; }

    public String getStudentName()                      { return studentName; }
    public void setStudentName(String n)                { this.studentName = n; }

    public String getStudentEmail()                     { return studentEmail; }
    public void setStudentEmail(String e)               { this.studentEmail = e; }

    public String getDepartment()                       { return department; }
    public void setDepartment(String d)                 { this.department = d; }

    public Integer getYear()                            { return year; }
    public void setYear(Integer y)                      { this.year = y; }

    public String getPhone()                            { return phone; }
    public void setPhone(String p)                      { this.phone = p; }

    public String getEventTitle()                       { return eventTitle; }
    public void setEventTitle(String t)                 { this.eventTitle = t; }

    public String getEventDate()                        { return eventDate; }
    public void setEventDate(String d)                  { this.eventDate = d; }

    public String getEventCategory()                    { return eventCategory; }
    public void setEventCategory(String c)              { this.eventCategory = c; }
}
