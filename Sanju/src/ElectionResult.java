
import java.time.LocalDateTime;

public class ElectionResult {
    private int resultId;
    private int electionId;
    private int candidateId;
    private String candidateName;
    private String symbol;
    private int voteCount;
    private double votePercentage;
    private int position;
    private boolean isWinner;
    private LocalDateTime generatedAt;

    public ElectionResult() {}

    public ElectionResult(int resultId, int electionId, int candidateId, 
                          String candidateName, String symbol, int voteCount, 
                          double votePercentage, int position, boolean isWinner, 
                          LocalDateTime generatedAt) {
        this.resultId = resultId;
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.symbol = symbol;
        this.voteCount = voteCount;
        this.votePercentage = votePercentage;
        this.position = position;
        this.isWinner = isWinner;
        this.generatedAt = generatedAt;
    }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

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

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public boolean isWinner() { return isWinner; }
    public void setWinner(boolean isWinner) { this.isWinner = isWinner; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
