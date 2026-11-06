

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for election creation, status management, and retrieval.
 */
public class ElectionService {

    private final ElectionDAO electionDAO = new ElectionDAO();

    public boolean createElection(Election election) throws Exception {
        if (!ValidationUtil.isNotEmpty(election.getElectionName())) {
            throw new IllegalArgumentException("Election name is required.");
        }
        if (!ValidationUtil.isValidElectionDates(election.getStartTime(), election.getEndTime())) {
            throw new IllegalArgumentException("End date/time must be strictly after start date/time.");
        }
        return electionDAO.createElection(election);
    }

    public boolean updateElection(Election election) throws Exception {
        if (!ValidationUtil.isNotEmpty(election.getElectionName())) {
            throw new IllegalArgumentException("Election name is required.");
        }
        if (!ValidationUtil.isValidElectionDates(election.getStartTime(), election.getEndTime())) {
            throw new IllegalArgumentException("End date/time must be strictly after start date/time.");
        }
        return electionDAO.updateElection(election);
    }

    public boolean startElection(int electionId) throws SQLException {
        Election election = electionDAO.getElectionById(electionId);
        if (election == null) {
            throw new IllegalArgumentException("Election not found.");
        }
        if ("CLOSED".equalsIgnoreCase(election.getStatus())) {
            throw new IllegalStateException("A CLOSED election cannot be restarted.");
        }
        return electionDAO.updateStatus(electionId, "ACTIVE", null);
    }

    public List<Election> getAllElections() throws SQLException {
        return electionDAO.getAllElections();
    }

    public Election getActiveElection() throws SQLException {
        return electionDAO.getActiveElection();
    }

    public Election getElectionById(int electionId) throws SQLException {
        return electionDAO.getElectionById(electionId);
    }

    public boolean deleteElection(int electionId) throws SQLException {
        return electionDAO.deleteElection(electionId);
    }
}
