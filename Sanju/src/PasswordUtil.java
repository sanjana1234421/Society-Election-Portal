
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing and verifying passwords using SHA-256.
 */
public class PasswordUtil {

    /**
     * Hashes a plain-text password using SHA-256 algorithm.
     * @param plainPassword Plain-text password.
     * @return Hexadecimal string representation of SHA-256 hash.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in JVM", e);
        }
    }

    /**
     * Verifies if an entered plain-text password matches the stored hash.
     * Supports both SHA-256 hex string comparison and fallback plain-text for legacy demo data.
     * @param plainPassword The password entered by the user.
     * @param storedHash The hashed string or stored value from the database.
     * @return true if credentials match, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String computedHash = hashPassword(plainPassword);
        if (computedHash.equalsIgnoreCase(storedHash)) {
            return true;
        }
        // Fallback for plain-text initial seed if any
        return plainPassword.equals(storedHash);
    }
}
