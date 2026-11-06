

import java.util.List;

/**
 * Controller mediating election lifecycle and poll closing.
 */
public class ElectionController {

    private final ElectionService electionService = new ElectionService();
    private final ResultService resultService = new ResultService();

    public boolean createElection(Election election) throws Exception {
        return electionService.createElection(election);
    }

    public boolean updateElection(Election election) throws Exception {
        return electionService.updateElection(election);
    }

    public boolean startPoll(int electionId) throws Exception {
        return electionService.startElection(electionId);
    }

    public ResultService.FinalElectionSummary closePoll(int electionId) throws Exception {
        return resultService.closeElectionAndDeclareWinner(electionId);
    }

    public List<Election> getAllElections() throws Exception {
        return electionService.getAllElections();
    }

    public Election getActiveElection() throws Exception {
        return electionService.getActiveElection();
    }
}
