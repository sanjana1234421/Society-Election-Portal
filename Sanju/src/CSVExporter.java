

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting election results to CSV format.
 */
public class CSVExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void exportResultsToCSV(File targetFile, Election election, List<ElectionResult> results, 
                                         int totalVoters, int totalVotes, String winnerStatusText) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            // Write Header Metadata
            writer.println("=================================================");
            writer.println("SOCIETY VOTING & ELECTION MANAGEMENT SYSTEM REPORT");
            writer.println("=================================================");
            writer.println("Report Generated At," + escapeCSV(LocalDateTime.now().format(DATE_FORMATTER)));
            writer.println("Election ID," + election.getElectionId());
            writer.println("Election Title," + escapeCSV(election.getElectionName()));
            writer.println("Description," + escapeCSV(election.getDescription()));
            writer.println("Election Status," + election.getStatus());
            writer.println("Start Time," + (election.getStartTime() != null ? election.getStartTime().format(DATE_FORMATTER) : "N/A"));
            writer.println("End Time," + (election.getEndTime() != null ? election.getEndTime().format(DATE_FORMATTER) : "N/A"));
            writer.println("Closed Time," + (election.getClosedAt() != null ? election.getClosedAt().format(DATE_FORMATTER) : "N/A"));
            writer.println();

            // Write Statistics Summary
            writer.println("--- TURNOUT STATISTICS ---");
            writer.println("Total Registered Voters," + totalVoters);
            writer.println("Total Votes Cast," + totalVotes);
            double turnout = totalVoters > 0 ? ((double) totalVotes / totalVoters) * 100.0 : 0.0;
            writer.printf("Voting Turnout Percentage,%.2f%%\n", turnout);
            writer.println("Remaining Non-Voters," + Math.max(0, totalVoters - totalVotes));
            writer.println("Final Result Status," + escapeCSV(winnerStatusText));
            writer.println();

            // Write Candidate Table
            writer.println("--- CANDIDATE RESULTS TABLE ---");
            writer.println("Rank,Candidate Name,Symbol,Votes Received,Vote Percentage,Winner Status");

            for (ElectionResult res : results) {
                writer.println(
                        res.getPosition() + "," +
                        escapeCSV(res.getCandidateName()) + "," +
                        escapeCSV(res.getSymbol()) + "," +
                        res.getVoteCount() + "," +
                        String.format("%.2f%%", res.getVotePercentage()) + "," +
                        (res.isWinner() ? "WINNER" : "RUNNER-UP/CONTENDER")
                );
            }

            writer.println();
            writer.println("--- END OF OFFICIAL ELECTION REPORT ---");
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
