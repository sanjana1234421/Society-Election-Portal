

import java.sql.*;

/**
 * Data Access Object for Admin authentication and management.
 */
public class AdminDAO {

    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT admin_id, username, password, full_name, created_at FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    return new Admin(
                            rs.getInt("admin_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            ts != null ? ts.toLocalDateTime() : null
                    );
                }
            }
        }
        return null;
    }

    public boolean createAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO admins (username, password, full_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getFullName());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        admin.setAdminId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updatePassword(int adminId, String hashedPassword) throws SQLException {
        String sql = "UPDATE admins SET password = ? WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, adminId);
            return stmt.executeUpdate() > 0;
        }
    }
}
