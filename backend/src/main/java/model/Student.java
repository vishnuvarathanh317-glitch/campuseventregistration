/**
 * Student — Concrete subclass of User.
 *
 * OOP concepts:
 *   - Inheritance (extends User)
 *   - Method overriding (validate(), toJson(), toString())
 *   - Additional student-specific fields
 */
public class Student extends User {

    private String department;
    private int    year;

    // ─── Constructors (overloaded) ────────────────────────────────────────────

    public Student() {
        super();
        setRole("student");
    }

    public Student(int id, String name, String email) {
        super(id, name, email, "student");
    }

    public Student(String name, String email, String password, String salt,
                   String phone, String department, int year) {
        super(name, email, password, salt, phone, "student");
        this.department = department;
        this.year       = year;
    }

    // ─── Overridden methods ───────────────────────────────────────────────────

    @Override
    public String validate() {
        if (ValidationUtil.isBlank(getName()))    return "Name is required.";
        String emailErr = ValidationUtil.validateEmail(getEmail());
        if (emailErr != null) return emailErr;
        if (ValidationUtil.isBlank(getPassword())) return "Password is required.";
        if (ValidationUtil.isBlank(department))   return "Department is required.";
        if (year < 1 || year > 5)                 return "Year must be between 1 and 5.";
        return null; // valid
    }

    @Override
    public String toJson() {
        return "{" + baseJsonFields() + "," +
               "\"department\":" + JsonUtil.quote(department) + "," +
               "\"year\":"       + year + "}";
    }

    @Override
    public String toString() {
        return "Student{id=" + getId() + ", name=" + getName() +
               ", dept=" + department + ", year=" + year + "}";
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getDepartment()              { return department; }
    public void setDepartment(String dept)     { this.department = dept; }

    public int getYear()                       { return year; }
    public void setYear(int year)              { this.year = year; }
}
