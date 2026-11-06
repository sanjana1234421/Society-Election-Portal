

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Candidates in an election.
 */
public class CandidateDAO {

    public List<Candidate> getCandidatesByElection(int electionId) throws SQLException {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT c.candidate_id, c.election_id, c.candidate_name, c.symbol, c.description, c.status, " +
                     "(SELECT COUNT(*) FROM votes v WHERE v.candidate_id = c.candidate_id AND v.election_id = c.election_id) AS vote_count " +
                     "FROM candidates c WHERE c.election_id = ? ORDER BY c.candidate_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Candidate c = new Candidate(
                            rs.getInt("candidate_id"),
                            rs.getInt("election_id"),
                            rs.getString("candidate_name"),
                            rs.getString("symbol"),
                            rs.getString("description"),
                            rs.getString("status")
                    );
                    c.setVoteCount(rs.getInt("vote_count"));
                    list.add(c);
                }
            }
        }
        return list;
    }

    public List<Candidate> getActiveCandidatesByElection(int electionId) throws SQLException {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT candidate_id, election_id, candidate_name, symbol, description, status " +
                     "FROM candidates WHERE election_id = ? AND status = 'ACTIVE' ORDER BY candidate_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, electionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Candidate(
                            rs.getInt("candidate_id"),
                            rs.getInt("election_id"),
                            rs.getString("candidate_name"),
                            rs.getString("symbol"),
                            rs.getString("description"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public Candidate findById(int candidateId) throws SQLException {
        String sql = "SELECT candidate_id, election_id, candidate_name, symbol, description, status " +
                     "FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Candidate(
                            rs.getInt("candidate_id"),
                            rs.getInt("election_id"),
                            rs.getString("candidate_name"),
                            rs.getString("symbol"),
                            rs.getString("description"),
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    public boolean createCandidate(Candidate candidate) throws SQLException {
        String sql = "INSERT INTO candidates (election_id, candidate_name, symbol, description, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, candidate.getElectionId());
            stmt.setString(2, candidate.getCandidateName().trim());
            stmt.setString(3, candidate.getSymbol().trim());
            stmt.setString(4, candidate.getDescription() != null ? candidate.getDescription().trim() : "");
            stmt.setString(5, candidate.getStatus() != null ? candidate.getStatus() : "ACTIVE");
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        candidate.setCandidateId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateCandidate(Candidate candidate) throws SQLException {
        String sql = "UPDATE candidates SET candidate_name = ?, symbol = ?, description = ?, status = ? " +
                     "WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, candidate.getCandidateName().trim());
            stmt.setString(2, candidate.getSymbol().trim());
            stmt.setString(3, candidate.getDescription() != null ? candidate.getDescription().trim() : "");
            stmt.setString(4, candidate.getStatus());
            stmt.setInt(5, candidate.getCandidateId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCandidate(int candidateId) throws SQLException {
        String sql = "DELETE FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidateId);
            return stmt.executeUpdate() > 0;
        }
    }
}
