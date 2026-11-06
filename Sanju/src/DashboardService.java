

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service providing real-time election statistics, voter turnout metrics,
 * and live candidate rankings for the Live Dashboard and Bar Chart.
 */
public class DashboardService {

    private final VoterDAO voterDAO = new VoterDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final VoteDAO voteDAO = new VoteDAO();

    public int getTotalRegisteredVoters() throws SQLException {
        return voterDAO.getTotalVotersCount();
    }

    public int getTotalVotesCast(int electionId) throws SQLException {
        return voteDAO.getTotalVotesForElection(electionId);
    }

    public int getRemainingVoters(int electionId) throws SQLException {
        int totalVoters = getTotalRegisteredVoters();
        int votesCast = getTotalVotesCast(electionId);
        return Math.max(0, totalVoters - votesCast);
    }

    public double getVotingPercentage(int electionId) throws SQLException {
        int totalVoters = getTotalRegisteredVoters();
        int votesCast = getTotalVotesCast(electionId);
        if (totalVoters == 0) return 0.0;
        return ((double) votesCast / totalVoters) * 100.0;
    }

    /**
     * Computes live statistics for all candidates in the given election.
     * Sorts descending by vote count and flags leader(s).
     */
    public List<LiveVoteStat> getLiveVoteStats(int electionId) throws SQLException {
        List<Candidate> candidates = candidateDAO.getActiveCandidatesByElection(electionId);
        Map<Integer, Integer> voteCounts = voteDAO.getVoteCountsByCandidate(electionId);
        int totalVotes = getTotalVotesCast(electionId);

        List<LiveVoteStat> stats = new ArrayList<>();
        int maxVotes = -1;

        for (Candidate c : candidates) {
            int count = voteCounts.getOrDefault(c.getCandidateId(), 0);
            double pct = totalVotes > 0 ? ((double) count / totalVotes) * 100.0 : 0.0;
            if (count > maxVotes) {
                maxVotes = count;
            }
            stats.add(new LiveVoteStat(c.getCandidateId(), c.getCandidateName(), c.getSymbol(), count, pct, 1, false));
        }

        // Sort descending by vote count
        stats.sort((a, b) -> Integer.compare(b.getVoteCount(), a.getVoteCount()));

        // Assign ranks (with standard competition ranking for ties) and leading flags
        int currentRank = 1;
        for (int i = 0; i < stats.size(); i++) {
            LiveVoteStat s = stats.get(i);
            if (i > 0 && s.getVoteCount() < stats.get(i - 1).getVoteCount()) {
                currentRank = i + 1;
            }
            s.setRank(currentRank);
            if (maxVotes > 0 && s.getVoteCount() == maxVotes) {
                s.setLeading(true);
            } else {
                s.setLeading(false);
            }
        }

        return stats;
    }

    /**
     * Determines the leading candidate or indicates a tie or no votes.
     */
    public String getLeadingCandidateSummary(int electionId) throws SQLException {
        List<LiveVoteStat> stats = getLiveVoteStats(electionId);
        if (stats.isEmpty()) {
            return "No Candidates Registered";
        }

        int totalVotes = getTotalVotesCast(electionId);
        if (totalVotes == 0) {
            return "No Votes Cast Yet";
        }

        int topVotes = stats.get(0).getVoteCount();
        List<String> leaders = new ArrayList<>();
        for (LiveVoteStat s : stats) {
            if (s.getVoteCount() == topVotes) {
                leaders.add(s.getCandidateName());
            }
        }

        if (leaders.size() == 1) {
            return stats.get(0).getCandidateName() + " (" + topVotes + " votes)";
        } else {
            return "TIE: " + String.join(" & ", leaders) + " (" + topVotes + " votes each)";
        }
    }
}
