
import java.time.LocalDateTime;

/**
 * Utility class for validating user input across the voting system.
 */
public class ValidationUtil {

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidVoterId(String voterId) {
        if (!isNotEmpty(voterId)) return false;
        String trimmed = voterId.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 50 && trimmed.matches("^[a-zA-Z0-9_-]+$");
    }

    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        String digits = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return digits.matches("^\\+?[0-9]{7,15}$");
    }

    public static boolean isValidFlatNumber(String flat) {
        if (!isNotEmpty(flat)) return false;
        return flat.trim().length() >= 1 && flat.trim().length() <= 20;
    }

    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) return false;
        return password.trim().length() >= 4;
    }

    public static boolean isValidElectionDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return false;
        return end.isAfter(start);
    }
}
