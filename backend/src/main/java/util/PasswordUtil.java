import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil — Password hashing using SHA-256 + per-user salt.
 * Uses Java's built-in MessageDigest (no external libraries).
 *
 * Security model:
 *   - Salt is 32 random bytes encoded as Base64 (unique per user)
 *   - Hash = SHA-256(salt + password)
 *   - Both salt and hash stored in DB separately
 */
public class PasswordUtil {

    private static final int SALT_BYTE_LENGTH = 32;

    /**
     * Generates a cryptographically random salt.
     * @return Base64-encoded salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[SALT_BYTE_LENGTH];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Hashes a password with the given salt.
     * @param password plaintext password
     * @param salt     Base64-encoded salt
     * @return hex-encoded SHA-256 hash
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = salt + password;
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plaintext password against a stored hash.
     * @param password    plaintext password to verify
     * @param salt        stored salt for the user
     * @param storedHash  stored hash from DB
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equalsIgnoreCase(storedHash);
    }

    /**
     * Validates password strength.
     * Requires: 8+ chars, at least one uppercase, lowercase, digit, special char.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c ->
            "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
