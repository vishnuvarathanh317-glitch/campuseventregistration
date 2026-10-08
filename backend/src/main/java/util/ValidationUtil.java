import java.util.regex.Pattern;

/**
 * ValidationUtil — Input validation for all form fields.
 * Demonstrates method overloading for different field types.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[6-9]\\d{9}$");  // Indian mobile numbers

    // ─── Required field checks ───────────────────────────────────────────────

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String requireField(String value, String fieldName) {
        if (isBlank(value)) return fieldName + " is required.";
        return null;
    }

    // ─── Email ───────────────────────────────────────────────────────────────

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static String validateEmail(String email) {
        if (isBlank(email))       return "Email is required.";
        if (!isValidEmail(email)) return "Please enter a valid email address.";
        return null;
    }

    // ─── Password (overloaded) ───────────────────────────────────────────────

    /** Basic validation: just check not blank */
    public static String validatePassword(String password) {
        if (isBlank(password)) return "Password is required.";
        return null;
    }

    /** Full validation: enforce strength rules (used during registration) */
    public static String validatePassword(String password, boolean enforceStrength) {
        if (isBlank(password)) return "Password is required.";
        if (!enforceStrength)  return null;
        if (password.length() < 8) return "Password must be at least 8 characters.";
        if (!PasswordUtil.isStrongPassword(password))
            return "Password must contain uppercase, lowercase, digit and special character.";
        return null;
    }

    // ─── Phone ───────────────────────────────────────────────────────────────

    public static String validatePhone(String phone) {
        if (isBlank(phone)) return null; // optional
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            return "Please enter a valid 10-digit mobile number.";
        return null;
    }

    // ─── Numeric ─────────────────────────────────────────────────────────────

    public static String validateYear(int year) {
        if (year < 1 || year > 5) return "Year must be between 1 and 5.";
        return null;
    }

    public static String validateCapacity(int capacity) {
        if (capacity < 1) return "Capacity must be at least 1.";
        if (capacity > 10000) return "Capacity cannot exceed 10,000.";
        return null;
    }

    // ─── Length ──────────────────────────────────────────────────────────────

    public static String validateLength(String value, String field, int min, int max) {
        if (isBlank(value)) return field + " is required.";
        int len = value.trim().length();
        if (len < min) return field + " must be at least " + min + " characters.";
        if (len > max) return field + " must not exceed " + max + " characters.";
        return null;
    }

    // ─── SQL injection basic guard ───────────────────────────────────────────

    /** Returns true if string contains dangerous SQL characters beyond PreparedStatement scope */
    public static boolean containsSQLInjection(String s) {
        if (s == null) return false;
        String upper = s.toUpperCase();
        return upper.contains("--") || upper.contains(";DROP") ||
               upper.contains(";DELETE") || upper.contains("EXEC(");
    }
}
