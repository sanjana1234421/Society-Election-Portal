

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Election management.
 */
public class ElectionDAO {

    public boolean createElection(Election election) throws SQLException {
        String sql = "INSERT INTO elections (election_name, description, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, election.getElectionName());
            stmt.setString(2, election.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(election.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(election.getEndTime()));
            stmt.setString(5, election.getStatus() != null ? election.getStatus() : "UPCOMING");
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        election.setElectionId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateElection(Election election) throws SQLException {
        String sql = "UPDATE elections SET election_name = ?, description = ?, start_time = ?, end_time = ?, status = ? " +
                     "WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, election.getElectionName());
            stmt.setString(2, election.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(election.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(election.getEndTime()));
            stmt.setString(5, election.getStatus());
            stmt.setInt(6, election.getElectionId());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Election> getAllElections() throws SQLException {
        List<Election> list = new ArrayList<>();
        String sql = "SELECT election_id, election_name, description, start_time, end_time, status, created_at, closed_at, winner_candidate_id " +
                     "FROM elections ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Election getActiveElection() throws SQLException {
        String sql = "SELECT election_id, election_name, description, start_time, end_time, status, created_at, closed_at, winner_candidate_id " +
                     "FROM elections WHERE status = 'ACTIVE' ORDER BY start_time DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    public Election getElectionById(int electionId) throws SQLException {
        String sql = "SELECT election_id, election_name, description, start_time, end_time, status, created_at, closed_at, winner_candidate_id " +
                     "FROM elections WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateStatus(int electionId, String status, LocalDateTime closedAt) throws SQLException {
        String sql = "UPDATE elections SET status = ?, closed_at = ? WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setTimestamp(2, closedAt != null ? Timestamp.valueOf(closedAt) : null);
            stmt.setInt(3, electionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateWinner(int electionId, Integer winnerCandidateId) throws SQLException {
        String sql = "UPDATE elections SET winner_candidate_id = ? WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (winnerCandidateId != null && winnerCandidateId > 0) {
                stmt.setInt(1, winnerCandidateId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, electionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteElection(int electionId) throws SQLException {
        String sql = "DELETE FROM elections WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Election mapRow(ResultSet rs) throws SQLException {
        Timestamp startTs = rs.getTimestamp("start_time");
        Timestamp endTs = rs.getTimestamp("end_time");
        Timestamp createTs = rs.getTimestamp("created_at");
        Timestamp closedTs = rs.getTimestamp("closed_at");
        int winnerIdVal = rs.getInt("winner_candidate_id");
        Integer winnerId = rs.wasNull() ? null : winnerIdVal;

        return new Election(
                rs.getInt("election_id"),
                rs.getString("election_name"),
                rs.getString("description"),
                startTs != null ? startTs.toLocalDateTime() : null,
                endTs != null ? endTs.toLocalDateTime() : null,
                rs.getString("status"),
                createTs != null ? createTs.toLocalDateTime() : null,
                closedTs != null ? closedTs.toLocalDateTime() : null,
                winnerId
        );
    }
}
