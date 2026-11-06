
import java.time.LocalDateTime;

public class Voter {
    private int voterId;
    private String voterUniqueId;
    private String name;
    private String phone;
    private String flatNumber;
    private String password;
    private String status; // 'ACTIVE', 'INACTIVE'
    private LocalDateTime createdAt;
    private boolean hasVoted; // helper field for UI status

    public Voter() {}

    public Voter(int voterId, String voterUniqueId, String name, String phone, 
                 String flatNumber, String password, String status, LocalDateTime createdAt) {
        this.voterId = voterId;
        this.voterUniqueId = voterUniqueId;
        this.name = name;
        this.phone = phone;
        this.flatNumber = flatNumber;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getVoterId() { return voterId; }
    public void setVoterId(int voterId) { this.voterId = voterId; }

    public String getVoterUniqueId() { return voterUniqueId; }
    public void setVoterUniqueId(String voterUniqueId) { this.voterUniqueId = voterUniqueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isHasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }
}
