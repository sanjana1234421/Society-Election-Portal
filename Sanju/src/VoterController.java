

import java.util.List;

/**
 * Controller mediating voter actions and ballot submission.
 */
public class VoterController {

    private final ElectionService electionService = new ElectionService();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final VotingService votingService = new VotingService();

    public Election getActiveBallot() throws Exception {
        return electionService.getActiveElection();
    }

    public List<Candidate> getCandidates(int electionId) throws Exception {
        return candidateDAO.getActiveCandidatesByElection(electionId);
    }

    public boolean checkIfAlreadyVoted(int electionId, int voterId) throws Exception {
        return votingService.hasVoterVoted(electionId, voterId);
    }

    public void castBallot(int electionId, int voterId, int candidateId) throws Exception {
        votingService.submitVote(electionId, voterId, candidateId);
    }
}
