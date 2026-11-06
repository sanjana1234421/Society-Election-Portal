

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for Votes and voting statistics.
 */
public class VoteDAO {

    public boolean hasVoterVoted(int electionId, int voterId) throws SQLException {
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

    public int getTotalVotesForElection(int electionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM votes WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Retrieves map of candidateId -> voteCount for a specific election.
     */
    public Map<Integer, Integer> getVoteCountsByCandidate(int electionId) throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT candidate_id, COUNT(*) AS count FROM votes WHERE election_id = ? GROUP BY candidate_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("candidate_id"), rs.getInt("count"));
                }
            }
        }
        return map;
    }

    /**
     * Inserts vote inside an existing transaction.
     */
    public boolean insertVoteTransactional(Connection conn, int electionId, int voterId, int candidateId) throws SQLException {
        String sql = "INSERT INTO votes (election_id, voter_id, candidate_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, electionId);
            stmt.setInt(2, voterId);
            stmt.setInt(3, candidateId);
            return stmt.executeUpdate() > 0;
        }
    }
}
