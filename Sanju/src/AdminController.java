

import java.util.List;

/**
 * Controller mediating administrative operations for candidates, voters, and metrics.
 */
public class AdminController {

    private final AdminDAO adminDAO = new AdminDAO();
    private final VoterDAO voterDAO = new VoterDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final DashboardService dashboardService = new DashboardService();

    public List<Voter> getAllVoters() throws Exception {
        return voterDAO.getAllVoters();
    }

    public List<Candidate> getCandidatesByElection(int electionId) throws Exception {
        return candidateDAO.getCandidatesByElection(electionId);
    }

    public int getTotalVoters() throws Exception {
        return dashboardService.getTotalRegisteredVoters();
    }

    public int getVotesCast(int electionId) throws Exception {
        return dashboardService.getTotalVotesCast(electionId);
    }
}
