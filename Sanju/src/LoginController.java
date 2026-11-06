

/**
 * Controller mediating authentication flows between Login view and services.
 */
public class LoginController {

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    public LoginController(AuthenticationService authService) {
        this.authService = authService;
    }

    public Admin authenticateAdmin(String username, String password) throws Exception {
        return authService.loginAdmin(username, password);
    }

    public Voter authenticateVoter(String voterUniqueId, String password) throws Exception {
        return authService.loginVoter(voterUniqueId, password);
    }
}
