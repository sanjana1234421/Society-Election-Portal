
import java.time.LocalDateTime;

public class Election {
    private int electionId;
    private String electionName;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // 'UPCOMING', 'ACTIVE', 'CLOSED'
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Integer winnerCandidateId;

    public Election() {}

    public Election(int electionId, String electionName, String description, 
                    LocalDateTime startTime, LocalDateTime endTime, String status, 
                    LocalDateTime createdAt, LocalDateTime closedAt, Integer winnerCandidateId) {
        this.electionId = electionId;
        this.electionName = electionName;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
        this.winnerCandidateId = winnerCandidateId;
    }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

    public String getElectionName() { return electionName; }
    public void setElectionName(String electionName) { this.electionName = electionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Integer getWinnerCandidateId() { return winnerCandidateId; }
    public void setWinnerCandidateId(Integer winnerCandidateId) { this.winnerCandidateId = winnerCandidateId; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public boolean isClosed() {
        return "CLOSED".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return electionId + " - " + electionName + " [" + status + "]";
    }
}
