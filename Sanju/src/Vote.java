
import java.time.LocalDateTime;

public class Vote {
    private int voteId;
    private int electionId;
    private int voterId;
    private int candidateId;
    private LocalDateTime votedAt;

    public Vote() {}

    public Vote(int voteId, int electionId, int voterId, int candidateId, LocalDateTime votedAt) {
        this.voteId = voteId;
        this.electionId = electionId;
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.votedAt = votedAt;
    }

    public int getVoteId() { return voteId; }
    public void setVoteId(int voteId) { this.voteId = voteId; }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

    public int getVoterId() { return voterId; }
    public void setVoterId(int voterId) { this.voterId = voterId; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }
}
