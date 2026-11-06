

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service managing vote submission with strict concurrency controls,
 * atomic database transactions, and double-voting prevention over LAN.
 */
public class VotingService {

    private final ElectionDAO electionDAO = new ElectionDAO();
    private final VoterDAO voterDAO = new VoterDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final VoteDAO voteDAO = new VoteDAO();

    /**
     * Submits a vote atomically using a database transaction.
     *
     * Steps executed within transaction:
     * 1. Check election exists and is ACTIVE.
     * 2. Check voter exists and status is ACTIVE.
     * 3. Check voter has not already voted in this election.
     * 4. Check candidate exists, belongs to this election, and is ACTIVE.
     * 5. Insert vote record.
     * 6. Commit transaction.
     *
     * @param electionId The target election ID.
     * @param voterId The voter ID.
     * @param candidateId The candidate selected.
     * @throws Exception If any validation or concurrency constraint fails.
     */
    public void submitVote(int electionId, int voterId, int candidateId) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Verify Election status
                String sqlElection = "SELECT status FROM elections WHERE election_id = ? FOR UPDATE";
                try (PreparedStatement stmt = conn.prepareStatement(sqlElection)) {
                    stmt.setInt(1, electionId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Election does not exist.");
                        }
                        String status = rs.getString("status");
                        if (!"ACTIVE".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("Voting is not currently allowed. Election status is " + status + ".");
                        }
                    }
                }

                // 2. Verify Voter status (Row-level lock prevents concurrent double voting attempts)
                String sqlVoter = "SELECT status FROM voters WHERE voter_id = ? FOR UPDATE";
                try (PreparedStatement stmt = conn.prepareStatement(sqlVoter)) {
                    stmt.setInt(1, voterId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Voter record not found.");
                        }
                        String status = rs.getString("status");
                        if (!"ACTIVE".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("Voter account is inactive and cannot cast a vote.");
                        }
                    }
                }

                // 3. Verify voter has not already voted
                String sqlCheckVote = "SELECT 1 FROM votes WHERE election_id = ? AND voter_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCheckVote)) {
                    stmt.setInt(1, electionId);
                    stmt.setInt(2, voterId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalStateException("Duplicate Vote Blocked: You have already cast your vote in this election.");
                        }
                    }
                }

                // 4. Validate Candidate
                String sqlCandidate = "SELECT status, election_id FROM candidates WHERE candidate_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCandidate)) {
                    stmt.setInt(1, candidateId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Selected candidate not found.");
                        }
                        int cElectionId = rs.getInt("election_id");
                        String status = rs.getString("status");
                        if (cElectionId != electionId) {
                            throw new IllegalArgumentException("Selected candidate does not belong to this election.");
                        }
                        if (!"ACTIVE".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("Selected candidate is inactive.");
                        }
                    }
                }

                // 5. Insert Vote (UNIQUE KEY uk_election_voter guarantees race protection)
                String sqlInsertVote = "INSERT INTO votes (election_id, voter_id, candidate_id) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertVote)) {
                    stmt.setInt(1, electionId);
                    stmt.setInt(2, voterId);
                    stmt.setInt(3, candidateId);
                    stmt.executeUpdate();
                }

                // 6. Commit
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                if (e.getErrorCode() == 1062 || (e.getMessage() != null && e.getMessage().contains("uk_election_voter"))) {
                    throw new IllegalStateException("Duplicate Vote Blocked: Vote has already been recorded for this voter.", e);
                }
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean hasVoterVoted(int electionId, int voterId) throws SQLException {
        return voteDAO.hasVoterVoted(electionId, voterId);
    }
}
