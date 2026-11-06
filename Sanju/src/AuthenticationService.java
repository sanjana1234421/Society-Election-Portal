

import java.sql.SQLException;

/**
 * Service handling authentication for Admins and Voters.
 */
public class AuthenticationService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final VoterDAO voterDAO = new VoterDAO();

    public Admin loginAdmin(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        Admin admin = adminDAO.findByUsername(username.trim());
        if (admin == null) {
            throw new IllegalArgumentException("Invalid admin credentials.");
        }

        if (!PasswordUtil.verifyPassword(password, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid admin credentials.");
        }

        return admin;
    }

    public Voter loginVoter(String voterUniqueId, String password) throws Exception {
        if (voterUniqueId == null || voterUniqueId.trim().isEmpty()) {
            throw new IllegalArgumentException("Voter ID cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password/PIN cannot be empty.");
        }

        Voter voter = voterDAO.findByUniqueId(voterUniqueId.trim());
        if (voter == null) {
            throw new IllegalArgumentException("Voter ID not found in society database.");
        }

        if (!voter.isActive()) {
            throw new IllegalStateException("Your voter account is currently INACTIVE. Please contact the society admin.");
        }

        if (!PasswordUtil.verifyPassword(password, voter.getPassword())) {
            throw new IllegalArgumentException("Incorrect password or PIN.");
        }

        return voter;
    }
}
