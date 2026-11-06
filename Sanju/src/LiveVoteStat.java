
/**
 * Data transfer object representing live candidate vote statistics
 * displayed on the real-time leaderboard and graphical bar chart.
 */
public class LiveVoteStat {
    private int candidateId;
    private String candidateName;
    private String symbol;
    private int voteCount;
    private double votePercentage;
    private int rank;
    private boolean isLeading;

    public LiveVoteStat() {}

    public LiveVoteStat(int candidateId, String candidateName, String symbol, 
                        int voteCount, double votePercentage, int rank, boolean isLeading) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.symbol = symbol;
        this.voteCount = voteCount;
        this.votePercentage = votePercentage;
        this.rank = rank;
        this.isLeading = isLeading;
    }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public double getVotePercentage() { return votePercentage; }
    public void setVotePercentage(double votePercentage) { this.votePercentage = votePercentage; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public boolean isLeading() { return isLeading; }
    public void setLeading(boolean leading) { isLeading = leading; }
}
