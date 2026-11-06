

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Main application entry screen providing modern Admin and Voter login
 * with LAN database configuration access.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService = new AuthenticationService();
    private boolean isAdminMode = true;

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    private final JLabel errorLabel;
    private final JButton adminTabBtn;
    private final JButton voterTabBtn;
    private final JButton loginBtn;

    public LoginFrame() {
        super("Society Voting & Election Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // Header Panel with title and subtitle
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR, getWidth(), getHeight(), new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(26, 24, 20, 24));

        JLabel appBadge = new JLabel("SECURE OFFLINE LAN VOTING SYSTEM");
        appBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        appBadge.setForeground(UIUtils.COLOR_PRIMARY);
        appBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appTitle = new JLabel("Society Election Portal");
        appTitle.setFont(UIUtils.FONT_TITLE);
        appTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appSubtitle = new JLabel("Green Valley Residential Society");
        appSubtitle.setFont(UIUtils.FONT_REGULAR);
        appSubtitle.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        appSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(appBadge);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(appTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(appSubtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Login Card
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        centerWrapper.setOpaque(false);

        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setPreferredSize(new Dimension(390, 370));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        // Tab Selector (Admin vs Voter)
        JPanel tabTogglePanel = new JPanel(new GridLayout(1, 2, 8, 0));
        tabTogglePanel.setOpaque(false);
        tabTogglePanel.setMaximumSize(new Dimension(360, 36));

        adminTabBtn = UIUtils.createPrimaryButton("Admin Login");
        voterTabBtn = UIUtils.createSecondaryButton("Voter Login");

        adminTabBtn.addActionListener(e -> switchRole(true));
        voterTabBtn.addActionListener(e -> switchRole(false));

        tabTogglePanel.add(adminTabBtn);
        tabTogglePanel.add(voterTabBtn);
        cardPanel.add(tabTogglePanel);
        cardPanel.add(Box.createVerticalStrut(20));

        // Username / Voter ID Field
        usernameLabel = new JLabel("Admin Username:");
        usernameLabel.setFont(UIUtils.FONT_BOLD);
        usernameLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(usernameLabel);
        cardPanel.add(Box.createVerticalStrut(6));

        usernameField = new JTextField();
        UIUtils.styleTextField(usernameField);
        usernameField.setMaximumSize(new Dimension(360, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(usernameField);
        cardPanel.add(Box.createVerticalStrut(14));

        // Password / PIN Field
        passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(UIUtils.FONT_BOLD);
        passwordLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(passwordLabel);
        cardPanel.add(Box.createVerticalStrut(6));

        passwordField = new JPasswordField();
        UIUtils.styleTextField(passwordField);
        passwordField.setMaximumSize(new Dimension(360, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(passwordField);
        cardPanel.add(Box.createVerticalStrut(8));

        // Error message label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIUtils.FONT_SMALL);
        errorLabel.setForeground(UIUtils.COLOR_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(errorLabel);
        cardPanel.add(Box.createVerticalStrut(12));

        // Login Button
        loginBtn = UIUtils.createPrimaryButton("Login as Administrator");
        loginBtn.setMaximumSize(new Dimension(360, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());
        cardPanel.add(loginBtn);

        // Enter key listeners
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // Footer Toolbar
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UIUtils.COLOR_SIDEBAR);
        footerPanel.setBorder(new EmptyBorder(10, 18, 10, 18));

        JButton lanConfigBtn = UIUtils.createSecondaryButton("LAN DB Setup");
        lanConfigBtn.setFont(UIUtils.FONT_SMALL);
        lanConfigBtn.addActionListener(e -> new DatabaseConfigDialog(this).setVisible(true));

        JButton exitBtn = UIUtils.createDangerButton("Exit");
        exitBtn.setFont(UIUtils.FONT_SMALL);
        exitBtn.addActionListener(e -> System.exit(0));

        JLabel serverHint = new JLabel("Host: " + DBConnection.getDbHost());
        serverHint.setFont(UIUtils.FONT_SMALL);
        serverHint.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFooter.setOpaque(false);
        leftFooter.add(lanConfigBtn);
        leftFooter.add(serverHint);

        footerPanel.add(leftFooter, BorderLayout.WEST);
        footerPanel.add(exitBtn, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);

        // Set initial admin defaults for demo convenience
        usernameField.setText("admin");
        passwordField.setText("admin123");
    }

    private void switchRole(boolean admin) {
        this.isAdminMode = admin;
        errorLabel.setText(" ");
        if (admin) {
            adminTabBtn.setBackground(UIUtils.COLOR_PRIMARY);
            adminTabBtn.setForeground(Color.WHITE);
            voterTabBtn.setBackground(UIUtils.COLOR_SURFACE_LIGHT);
            voterTabBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

            usernameLabel.setText("Admin Username:");
            passwordLabel.setText("Admin Password:");
            loginBtn.setText("Login as Administrator");
            usernameField.setText("admin");
            passwordField.setText("admin123");
        } else {
            voterTabBtn.setBackground(UIUtils.COLOR_PRIMARY);
            voterTabBtn.setForeground(Color.WHITE);
            adminTabBtn.setBackground(UIUtils.COLOR_SURFACE_LIGHT);
            adminTabBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

            usernameLabel.setText("Voter Unique ID:");
            passwordLabel.setText("Voter Password / PIN:");
            loginBtn.setText("Login as Voter");
            usernameField.setText("VOTER001");
            passwordField.setText("1234");
        }
    }

    private void handleLogin() {
        errorLabel.setText(" ");
        String idOrUser = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        loginBtn.setEnabled(false);
        loginBtn.setText("Authenticating...");

        SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                if (isAdminMode) {
                    return authService.loginAdmin(idOrUser, pass);
                } else {
                    return authService.loginVoter(idOrUser, pass);
                }
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText(isAdminMode ? "Login as Administrator" : "Login as Voter");
                try {
                    Object result = get();
                    if (isAdminMode && result instanceof Admin) {
                        Admin admin = (Admin) result;
                        dispose();
                        SwingUtilities.invokeLater(() -> new AdminDashboard(admin).setVisible(true));
                    } else if (!isAdminMode && result instanceof Voter) {
                        Voter voter = (Voter) result;
                        dispose();
                        SwingUtilities.invokeLater(() -> new VoterDashboard(voter).setVisible(true));
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause.getMessage();
                    if (message != null && message.contains("Communications link failure")) {
                        message = "Could not reach MySQL on " + DBConnection.getDbHost() + ".\nClick 'LAN DB Setup' to check host IP.";
                    }
                    errorLabel.setText(message != null && message.length() > 45 ? message.substring(0, 42) + "..." : message);
                    JOptionPane.showMessageDialog(LoginFrame.this, message, "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
