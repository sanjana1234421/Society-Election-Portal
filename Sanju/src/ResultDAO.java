

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for persisting and reading official final election results.
 */
public class ResultDAO {

    public boolean saveResults(List<ElectionResult> results) throws SQLException {
        if (results == null || results.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO election_results (election_id, candidate_id, vote_count, vote_percentage, position, is_winner) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (ElectionResult r : results) {
                    stmt.setInt(1, r.getElectionId());
                    stmt.setInt(2, r.getCandidateId());
                    stmt.setInt(3, r.getVoteCount());
                    stmt.setDouble(4, r.getVotePercentage());
                    stmt.setInt(5, r.getPosition());
                    stmt.setBoolean(6, r.isWinner());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<ElectionResult> getResultsByElection(int electionId) throws SQLException {
        List<ElectionResult> list = new ArrayList<>();
        String sql = "SELECT r.result_id, r.election_id, r.candidate_id, c.candidate_name, c.symbol, " +
                     "r.vote_count, r.vote_percentage, r.position, r.is_winner, r.generated_at " +
                     "FROM election_results r " +
                     "JOIN candidates c ON r.candidate_id = c.candidate_id " +
                     "WHERE r.election_id = ? " +
                     "ORDER BY r.position ASC, r.vote_count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp genTs = rs.getTimestamp("generated_at");
                    list.add(new ElectionResult(
                            rs.getInt("result_id"),
                            rs.getInt("election_id"),
                            rs.getInt("candidate_id"),
                            rs.getString("candidate_name"),
                            rs.getString("symbol"),
                            rs.getInt("vote_count"),
                            rs.getDouble("vote_percentage"),
                            rs.getInt("position"),
                            rs.getBoolean("is_winner"),
                            genTs != null ? genTs.toLocalDateTime() : null
                    ));
                }
            }
        }
        return list;
    }

    public boolean clearResultsForElection(int electionId) throws SQLException {
        String sql = "DELETE FROM election_results WHERE election_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            return stmt.executeUpdate() >= 0;
        }
    }
}
