/**
 * Admin — Concrete subclass of User.
 *
 * OOP concepts:
 *   - Inheritance (extends User)
 *   - Method overriding (validate(), toJson())
 *   - Polymorphism: Admin instance can be used wherever User is expected
 */
public class Admin extends User {

    // ─── Constructors (overloaded) ────────────────────────────────────────────

    public Admin() {
        super();
        setRole("admin");
    }

    public Admin(int id, String name, String email) {
        super(id, name, email, "admin");
    }

    public Admin(String name, String email, String password, String salt, String phone) {
        super(name, email, password, salt, phone, "admin");
    }

    // ─── Overridden methods ───────────────────────────────────────────────────

    @Override
    public String validate() {
        if (ValidationUtil.isBlank(getName()))     return "Admin name is required.";
        String emailErr = ValidationUtil.validateEmail(getEmail());
        if (emailErr != null) return emailErr;
        if (ValidationUtil.isBlank(getPassword())) return "Password is required.";
        return null;
    }

    @Override
    public String toJson() {
        return "{" + baseJsonFields() + "," +
               "\"isAdmin\":true}";
    }

    @Override
    public String toString() {
        return "Admin{id=" + getId() + ", name=" + getName() + ", email=" + getEmail() + "}";
    }

    /** Admin-only: check if this admin has a given permission (extendable) */
    public boolean hasPermission(String permission) {
        // All admins have all permissions in this version
        return true;
    }
}
