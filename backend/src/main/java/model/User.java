import java.time.LocalDateTime;

/**
 * User — Abstract base class demonstrating OOP abstraction and encapsulation.
 *
 * OOP concepts:
 *   - Abstract class (cannot be instantiated directly)
 *   - Encapsulation (private fields, public getters/setters)
 *   - Abstract method validate() — forces subclasses to implement
 */
public abstract class User {

    // ─── Encapsulated fields ─────────────────────────────────────────────────
    private int           id;
    private String        name;
    private String        email;
    private String        password;   // stored hash
    private String        salt;
    private String        phone;
    private String        role;
    private LocalDateTime createdAt;

    // ─── Constructors (overloaded) ────────────────────────────────────────────

    public User() {}

    public User(int id, String name, String email, String role) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.role  = role;
    }

    public User(String name, String email, String password, String salt, String phone, String role) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.salt     = salt;
        this.phone    = phone;
        this.role     = role;
    }

    // ─── Abstract methods ─────────────────────────────────────────────────────

    /** Each subclass validates its own required fields */
    public abstract String validate();

    /** Returns a JSON representation of the user (public-safe) */
    public abstract String toJson();

    // ─── Common toJson helper ─────────────────────────────────────────────────

    protected String baseJsonFields() {
        return "\"id\":"         + id                             + "," +
               "\"name\":"       + JsonUtil.quote(name)           + "," +
               "\"email\":"      + JsonUtil.quote(email)          + "," +
               "\"phone\":"      + JsonUtil.quote(phone)          + "," +
               "\"role\":"       + JsonUtil.quote(role)           + "," +
               "\"createdAt\":"  + JsonUtil.quote(createdAt != null ? createdAt.toString() : "");
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    public String getPassword()          { return password; }
    public void setPassword(String p)    { this.password = p; }

    public String getSalt()              { return salt; }
    public void setSalt(String salt)     { this.salt = salt; }

    public String getPhone()             { return phone; }
    public void setPhone(String phone)   { this.phone = phone; }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + name + ", email=" + email + ", role=" + role + "}";
    }
}
