

import java.util.ArrayList;
import java.util.List;

/**
 * Offline AI-Assisted Insights and Statistical Natural Language Analysis Service.
 * Analyzes election outcome, voter mobilization, winning margins, and voter turnout
 * completely offline without external APIs or cloud dependencies.
 */
public class AIInsightsService {

    /**
     * Generates a comprehensive natural language analytical report based on election data.
     */
    public static String generateElectionInsights(ResultService.FinalElectionSummary summary) {
        if (summary == null || summary.getElection() == null) {
            return "No election data available to generate AI insights.";
        }

        StringBuilder sb = new StringBuilder();
        int totalVotes = summary.getTotalVotesCast();
        int totalVoters = summary.getTotalRegisteredVoters();
        double turnout = summary.getTurnoutPercentage();
        List<ElectionResult> results = summary.getResults();

        sb.append("=== AI ELECTION INSIGHTS & STATISTICAL ANALYSIS ===\n\n");

        if (totalVotes == 0) {
            sb.append(" Turnout Analysis: Election concluded with 0% participation (no votes cast).\n");
            sb.append(" Recommendation: Review voter notification channels and schedule a re-poll.\n");
            return sb.toString();
        }

        // 1. Executive Summary
        if (summary.isTie()) {
            if (summary.getTiedWinners() != null && !summary.getTiedWinners().isEmpty()) {
                sb.append(" Executive Outcome: The election resulted in a historic DEADLOCK TIE between ");
                List<String> tiedNames = new ArrayList<>();
                int tieVotes = 0;
                double tiePct = 0;
                for (ElectionResult r : summary.getTiedWinners()) {
                    tiedNames.add(r.getCandidateName());
                    tieVotes = r.getVoteCount();
                    tiePct = r.getVotePercentage();
                }
                sb.append(String.join(" and ", tiedNames))
                  .append(", each securing ").append(tieVotes).append(" votes (")
                  .append(String.format("%.1f%%", tiePct)).append(" of all ballots cast).\n");
                sb.append(" Governance Directive: Society bylaws require either a run-off ballot or designated tie-breaking committee vote.\n");
            } else {
                sb.append(" Executive Outcome: The election concluded in a tie.\n");
            }
        } else if (summary.getWinner() != null) {
            ElectionResult winner = summary.getWinner();
            sb.append(" Executive Outcome: ").append(winner.getCandidateName())
              .append(" won the election decisively with ").append(winner.getVoteCount())
              .append(" votes, securing ").append(String.format("%.1f%%", winner.getVotePercentage()))
              .append(" of the total votes.\n");

            if (summary.getRunnerUp() != null) {
                ElectionResult runnerUp = summary.getRunnerUp();
                int marginVotes = winner.getVoteCount() - runnerUp.getVoteCount();
                double marginPct = winner.getVotePercentage() - runnerUp.getVotePercentage();
                sb.append(" Winning Margin: The winning lead over runner-up ")
                  .append(runnerUp.getCandidateName()).append(" was ")
                  .append(marginVotes).append(" vote(s) (")
                  .append(String.format("%.1f%%", marginPct)).append(" margin).\n");
            }
        }

        sb.append("\n");

        // 2. Voter Mobilization and Turnout Quality
        sb.append(" Turnout Quality: Overall society voter participation reached ")
          .append(String.format("%.1f%%", turnout))
          .append(" (").append(totalVotes).append(" out of ").append(totalVoters).append(" registered residents voted).\n");

        if (turnout >= 80.0) {
            sb.append("  Assessment: Exceptional civic engagement. High mandate validity.\n");
        } else if (turnout >= 60.0) {
            sb.append("  Assessment: Healthy representative turnout across resident blocks.\n");
        } else if (turnout >= 40.0) {
            sb.append("  Assessment: Moderate resident participation. Consider mobile voting reminders in future.\n");
        } else {
            sb.append("  Assessment: Low turnout (<40%). Community outreach recommended for broader consensus.\n");
        }

        // 3. Competitive Balance Metrics
        if (results.size() > 1) {
            ElectionResult highest = results.get(0);
            ElectionResult lowest = results.get(results.size() - 1);
            sb.append(" Vote Distribution: Highest candidate captured ")
              .append(String.format("%.1f%%", highest.getVotePercentage()))
              .append(" while lowest candidate captured ")
              .append(String.format("%.1f%%", lowest.getVotePercentage()))
              .append(".\n");

            if (!summary.isTie() && summary.getRunnerUp() != null) {
                int margin = summary.getWinner().getVoteCount() - summary.getRunnerUp().getVoteCount();
                if (margin <= 2) {
                    sb.append(" Competitiveness: EXTREMELY CLOSE CONTEST. A swing of just 1-2 votes would have altered the outcome.\n");
                } else if (margin <= 10) {
                    sb.append(" Competitiveness: HIGHLY COMPETITIVE. Active engagement observed between leading candidates.\n");
                } else {
                    sb.append(" Competitiveness: CLEAR MANDATE. The winning candidate established a strong governing majority.\n");
                }
            }
        }

        return sb.toString();
    }
}
