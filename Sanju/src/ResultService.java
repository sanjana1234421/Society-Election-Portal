

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service managing Automatic Winner Declaration, Tie Resolution,
 * and Official Election Finalization.
 */
public class ResultService {

    private final ElectionDAO electionDAO = new ElectionDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final VoteDAO voteDAO = new VoteDAO();
    private final ResultDAO resultDAO = new ResultDAO();
    private final VoterDAO voterDAO = new VoterDAO();

    /**
     * DTO containing all final outcome metrics for the closed election.
     */
    public static class FinalElectionSummary {
        private Election election;
        private List<ElectionResult> results = new ArrayList<>();
        private boolean isTie;
        private ElectionResult winner;
        private List<ElectionResult> tiedWinners = new ArrayList<>();
        private ElectionResult runnerUp;
        private int totalRegisteredVoters;
        private int totalVotesCast;
        private double turnoutPercentage;
        private String statusMessage;

        // Getters and Setters
        public Election getElection() { return election; }
        public void setElection(Election election) { this.election = election; }

        public List<ElectionResult> getResults() { return results; }
        public void setResults(List<ElectionResult> results) { this.results = results; }

        public boolean isTie() { return isTie; }
        public void setTie(boolean tie) { isTie = tie; }

        public ElectionResult getWinner() { return winner; }
        public void setWinner(ElectionResult winner) { this.winner = winner; }

        public List<ElectionResult> getTiedWinners() { return tiedWinners; }
        public void setTiedWinners(List<ElectionResult> tiedWinners) { this.tiedWinners = tiedWinners; }

        public ElectionResult getRunnerUp() { return runnerUp; }
        public void setRunnerUp(ElectionResult runnerUp) { this.runnerUp = runnerUp; }

        public int getTotalRegisteredVoters() { return totalRegisteredVoters; }
        public void setTotalRegisteredVoters(int totalRegisteredVoters) { this.totalRegisteredVoters = totalRegisteredVoters; }

        public int getTotalVotesCast() { return totalVotesCast; }
        public void setTotalVotesCast(int totalVotesCast) { this.totalVotesCast = totalVotesCast; }

        public double getTurnoutPercentage() { return turnoutPercentage; }
        public void setTurnoutPercentage(double turnoutPercentage) { this.turnoutPercentage = turnoutPercentage; }

        public String getStatusMessage() { return statusMessage; }
        public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    }

    /**
     * Closes the election and executes the automatic winner calculation logic.
     * Persists calculated results to the database.
     *
     * @param electionId Target election ID.
     * @return FinalElectionSummary containing complete calculated results.
     * @throws Exception If calculation or database update fails.
     */
    public synchronized FinalElectionSummary closeElectionAndDeclareWinner(int electionId) throws Exception {
        Election election = electionDAO.getElectionById(electionId);
        if (election == null) {
            throw new IllegalArgumentException("Election ID " + electionId + " not found.");
        }

        LocalDateTime closedAt = LocalDateTime.now();

        // 1. Mark election as CLOSED
        electionDAO.updateStatus(electionId, "CLOSED", closedAt);
        election.setStatus("CLOSED");
        election.setClosedAt(closedAt);

        // 2. Fetch candidates and vote counts
        List<Candidate> candidates = candidateDAO.getActiveCandidatesByElection(electionId);
        Map<Integer, Integer> voteCounts = voteDAO.getVoteCountsByCandidate(electionId);
        int totalVotes = voteDAO.getTotalVotesForElection(electionId);
        int totalVoters = voterDAO.getTotalVotersCount();

        // 3. Clear any previous results for this election to ensure idempotency
        resultDAO.clearResultsForElection(electionId);

        // 4. Calculate counts and percentages
        List<ElectionResult> resultList = new ArrayList<>();
        int maxVotes = -1;

        for (Candidate c : candidates) {
            int count = voteCounts.getOrDefault(c.getCandidateId(), 0);
            double pct = totalVotes > 0 ? ((double) count / totalVotes) * 100.0 : 0.0;
            if (count > maxVotes) {
                maxVotes = count;
            }
            ElectionResult er = new ElectionResult();
            er.setElectionId(electionId);
            er.setCandidateId(c.getCandidateId());
            er.setCandidateName(c.getCandidateName());
            er.setSymbol(c.getSymbol());
            er.setVoteCount(count);
            er.setVotePercentage(Math.round(pct * 100.0) / 100.0);
            er.setGeneratedAt(closedAt);
            resultList.add(er);
        }

        // 5. Sort candidates descending by vote count
        resultList.sort((a, b) -> Integer.compare(b.getVoteCount(), a.getVoteCount()));

        // 6. Detect Winner vs. Tie
        FinalElectionSummary summary = new FinalElectionSummary();
        summary.setElection(election);
        summary.setTotalVotesCast(totalVotes);
        summary.setTotalRegisteredVoters(totalVoters);
        double turnout = totalVoters > 0 ? ((double) totalVotes / totalVoters) * 100.0 : 0.0;
        summary.setTurnoutPercentage(Math.round(turnout * 100.0) / 100.0);

        List<ElectionResult> tiedCandidates = new ArrayList<>();
        for (ElectionResult r : resultList) {
            if (maxVotes > 0 && r.getVoteCount() == maxVotes) {
                tiedCandidates.add(r);
            }
        }

        Integer winnerCandidateId = null;

        if (totalVotes == 0) {
            summary.setTie(true);
            summary.setStatusMessage("ELECTION CLOSED: NO VOTES CAST");
            for (ElectionResult r : resultList) {
                r.setPosition(1);
                r.setWinner(false);
            }
        } else if (tiedCandidates.size() > 1) {
            // TIE DETECTED
            summary.setTie(true);
            summary.setTiedWinners(tiedCandidates);
            summary.setStatusMessage("ELECTION RESULT: TIE");

            // Standard competition ranking (e.g. 1, 1, 3, 4)
            int currentRank = 1;
            for (int i = 0; i < resultList.size(); i++) {
                ElectionResult r = resultList.get(i);
                if (i > 0 && r.getVoteCount() < resultList.get(i - 1).getVoteCount()) {
                    currentRank = i + 1;
                }
                r.setPosition(currentRank);
                r.setWinner(false); // No single winner in a tie
            }
        } else if (tiedCandidates.size() == 1) {
            // CLEAR WINNER
            summary.setTie(false);
            ElectionResult winner = tiedCandidates.get(0);
            winner.setWinner(true);
            summary.setWinner(winner);
            summary.setStatusMessage("WINNER DECLARED: " + winner.getCandidateName());
            winnerCandidateId = winner.getCandidateId();

            int currentRank = 1;
            for (int i = 0; i < resultList.size(); i++) {
                ElectionResult r = resultList.get(i);
                if (i > 0 && r.getVoteCount() < resultList.get(i - 1).getVoteCount()) {
                    currentRank = i + 1;
                }
                r.setPosition(currentRank);
                r.setWinner(i == 0);
                if (i == 1) {
                    summary.setRunnerUp(r);
                }
            }
        }

        // 7. Persist results in election_results table
        resultDAO.saveResults(resultList);

        // 8. Update winner_candidate_id on elections table
        electionDAO.updateWinner(electionId, winnerCandidateId);
        election.setWinnerCandidateId(winnerCandidateId);

        summary.setResults(resultList);
        return summary;
    }

    /**
     * Loads existing final summary for an already closed election.
     */
    public FinalElectionSummary getFinalElectionSummary(int electionId) throws SQLException {
        Election election = electionDAO.getElectionById(electionId);
        if (election == null) return null;

        List<ElectionResult> results = resultDAO.getResultsByElection(electionId);
        if (results.isEmpty() && "CLOSED".equalsIgnoreCase(election.getStatus())) {
            // If closed but results weren't calculated, calculate now
            try {
                return closeElectionAndDeclareWinner(electionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int totalVotes = voteDAO.getTotalVotesForElection(electionId);
        int totalVoters = voterDAO.getTotalVotersCount();

        FinalElectionSummary summary = new FinalElectionSummary();
        summary.setElection(election);
        summary.setResults(results);
        summary.setTotalVotesCast(totalVotes);
        summary.setTotalRegisteredVoters(totalVoters);
        double turnout = totalVoters > 0 ? ((double) totalVotes / totalVoters) * 100.0 : 0.0;
        summary.setTurnoutPercentage(Math.round(turnout * 100.0) / 100.0);

        if (!results.isEmpty()) {
            int topVotes = results.get(0).getVoteCount();
            List<ElectionResult> topList = new ArrayList<>();
            for (ElectionResult r : results) {
                if (r.getVoteCount() == topVotes && topVotes > 0) {
                    topList.add(r);
                }
            }

            if (topList.size() > 1 || topVotes == 0) {
                summary.setTie(true);
                summary.setTiedWinners(topList);
                summary.setStatusMessage(topVotes == 0 ? "ELECTION CLOSED: NO VOTES CAST" : "ELECTION RESULT: TIE");
            } else {
                summary.setTie(false);
                summary.setWinner(results.get(0));
                summary.setStatusMessage("WINNER DECLARED: " + results.get(0).getCandidateName());
                if (results.size() > 1) {
                    summary.setRunnerUp(results.get(1));
                }
            }
        }

        return summary;
    }
}
