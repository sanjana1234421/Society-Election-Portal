

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Voter entity.
 */
public class VoterDAO {

    public Voter findByUniqueId(String voterUniqueId) throws SQLException {
        String sql = "SELECT voter_id, voter_unique_id, name, phone, flat_number, password, status, created_at " +
                     "FROM voters WHERE voter_unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, voterUniqueId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Voter findById(int voterId) throws SQLException {
        String sql = "SELECT voter_id, voter_unique_id, name, phone, flat_number, password, status, created_at " +
                     "FROM voters WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, voterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Voter> getAllVoters() throws SQLException {
        List<Voter> list = new ArrayList<>();
        String sql = "SELECT voter_id, voter_unique_id, name, phone, flat_number, password, status, created_at " +
                     "FROM voters ORDER BY flat_number ASC, name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Voter> searchVoters(String query) throws SQLException {
        List<Voter> list = new ArrayList<>();
        String sql = "SELECT voter_id, voter_unique_id, name, phone, flat_number, password, status, created_at " +
                     "FROM voters WHERE voter_unique_id LIKE ? OR name LIKE ? OR flat_number LIKE ? " +
                     "ORDER BY flat_number ASC, name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean createVoter(Voter voter) throws SQLException {
        String sql = "INSERT INTO voters (voter_unique_id, name, phone, flat_number, password, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, voter.getVoterUniqueId().trim());
            stmt.setString(2, voter.getName().trim());
            stmt.setString(3, voter.getPhone().trim());
            stmt.setString(4, voter.getFlatNumber().trim());
            stmt.setString(5, voter.getPassword());
            stmt.setString(6, voter.getStatus() != null ? voter.getStatus() : "ACTIVE");
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        voter.setVoterId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateVoter(Voter voter) throws SQLException {
        String sql = "UPDATE voters SET name = ?, phone = ?, flat_number = ?, status = ? WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, voter.getName().trim());
            stmt.setString(2, voter.getPhone().trim());
            stmt.setString(3, voter.getFlatNumber().trim());
            stmt.setString(4, voter.getStatus());
            stmt.setInt(5, voter.getVoterId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int voterId, String status) throws SQLException {
        String sql = "UPDATE voters SET status = ? WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, voterId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean resetPassword(int voterId, String newHashedPassword) throws SQLException {
        String sql = "UPDATE voters SET password = ? WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, voterId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getTotalVotersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM voters WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean hasVoted(int electionId, int voterId) throws SQLException {
        String sql = "SELECT 1 FROM votes WHERE election_id = ? AND voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            stmt.setInt(2, voterId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasVoterVoted(int electionId, int voterId) throws SQLException {
        return hasVoted(electionId, voterId);
    }

    private Voter mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Voter(
                rs.getInt("voter_id"),
                rs.getString("voter_unique_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("flat_number"),
                rs.getString("password"),
                rs.getString("status"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
