
public class Candidate {
    private int candidateId;
    private int electionId;
    private String candidateName;
    private String symbol;
    private String description;
    private String status; // 'ACTIVE', 'INACTIVE'
    private int voteCount; // helper field for UI

    public Candidate() {}

    public Candidate(int candidateId, int electionId, String candidateName, 
                     String symbol, String description, String status) {
        this.candidateId = candidateId;
        this.electionId = electionId;
        this.candidateName = candidateName;
        this.symbol = symbol;
        this.description = description;
        this.status = status;
    }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    @Override
    public String toString() {
        return candidateName + " (" + symbol + ")";
    }
}
