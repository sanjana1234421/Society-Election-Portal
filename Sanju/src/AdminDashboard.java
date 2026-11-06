

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Administrator Dashboard providing sidebar navigation, high-level metrics,
 * election configuration, live vote tracking, and final winner declaration.
 */
public class AdminDashboard extends JFrame {

    private final Admin currentAdmin;
    private final ElectionService electionService = new ElectionService();
    private final DashboardService dashboardService = new DashboardService();

    private final JPanel contentArea;
    private final CardLayout cardLayout;

    private final JPanel dashboardOverviewPanel;
    private final ElectionPanel electionPanel;
    private final CandidatePanel candidatePanel;
    private final VoterPanel voterPanel;
    private final LiveResultsPanel liveResultsPanel;
    private final FinalResultPanel finalResultPanel;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentCard = "DASHBOARD";

    // Overview Cards
    private StatCard totalVotersCard;
    private StatCard votesCastCard;
    private StatCard votesRemainingCard;
    private StatCard turnoutCard;
    private StatCard electionStatusCard;
    private StatCard leadingCandidateCard;
    private JLabel activeElectionBanner;

    public AdminDashboard(Admin admin) {
        super("Society Election Management System - Administrator Portal");
        this.currentAdmin = admin;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // Stop live polling when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (liveResultsPanel != null) {
                    liveResultsPanel.stopLivePolling();
                }
            }
        });

        // 1. Sidebar Navigation
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Main Content Area with CardLayout
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIUtils.COLOR_BG_DARK);

        // Child Panels
        dashboardOverviewPanel = createOverviewPanel();
        finalResultPanel = new FinalResultPanel();
        liveResultsPanel = new LiveResultsPanel();
        electionPanel = new ElectionPanel(electionId -> {
            // When poll is closed, switch automatically to Final Results
            finalResultPanel.loadElections();
            finalResultPanel.selectElectionById(electionId);
            switchNav("FINAL_RESULTS");
        });
        candidatePanel = new CandidatePanel();
        voterPanel = new VoterPanel();

        contentArea.add(dashboardOverviewPanel, "DASHBOARD");
        contentArea.add(electionPanel, "ELECTIONS");
        contentArea.add(candidatePanel, "CANDIDATES");
        contentArea.add(voterPanel, "VOTERS");
        contentArea.add(liveResultsPanel, "LIVE_RESULTS");
        contentArea.add(finalResultPanel, "FINAL_RESULTS");

        add(contentArea, BorderLayout.CENTER);

        refreshOverviewStats();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIUtils.COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 700));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.COLOR_BORDER));

        // Brand / Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel title = new JLabel("ELECTION ADMIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Society Portal (LAN)");
        subtitle.setFont(UIUtils.FONT_SMALL);
        subtitle.setForeground(UIUtils.COLOR_PRIMARY);

        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(subtitle);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Nav Items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        menuPanel.add(createNavButton("[ ] Dashboard", "DASHBOARD"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton(" Elections", "ELECTIONS"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton("[+] Candidates", "CANDIDATES"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton("[ ] Voters", "VOTERS"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton(" Live Results", "LIVE_RESULTS"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton(" Final Results", "FINAL_RESULTS"));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Footer with Admin Profile and Settings
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(14, 16, 20, 16));

        JLabel adminName = new JLabel(currentAdmin != null ? currentAdmin.getFullName() : "Administrator");
        adminName.setFont(UIUtils.FONT_BOLD);
        adminName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel adminRole = new JLabel("LAN Host: " + DBConnection.getDbHost());
        adminRole.setFont(UIUtils.FONT_SMALL);
        adminRole.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JButton settingsBtn = UIUtils.createSecondaryButton(" DB Settings");
        settingsBtn.setMaximumSize(new Dimension(200, 32));
        settingsBtn.setFont(UIUtils.FONT_SMALL);
        settingsBtn.addActionListener(e -> new DatabaseConfigDialog(this).setVisible(true));

        JButton logoutBtn = UIUtils.createDangerButton("[<-] Logout");
        logoutBtn.setMaximumSize(new Dimension(200, 32));
        logoutBtn.setFont(UIUtils.FONT_SMALL);
        logoutBtn.addActionListener(e -> logout());

        userPanel.add(adminName);
        userPanel.add(Box.createVerticalStrut(2));
        userPanel.add(adminRole);
        userPanel.add(Box.createVerticalStrut(12));
        userPanel.add(settingsBtn);
        userPanel.add(Box.createVerticalStrut(8));
        userPanel.add(logoutBtn);

        sidebar.add(userPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String text, String cardKey) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BOLD);
        btn.setMaximumSize(new Dimension(216, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));

        boolean isActive = "DASHBOARD".equals(cardKey);
        btn.setForeground(isActive ? Color.WHITE : UIUtils.COLOR_TEXT_SECONDARY);
        btn.setOpaque(isActive);
        btn.setBackground(isActive ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_SIDEBAR);

        btn.addActionListener(e -> switchNav(cardKey));
        navButtons.put(cardKey, btn);
        return btn;
    }

    public void switchNav(String cardKey) {
        // Stop polling if moving away from live results
        if ("LIVE_RESULTS".equals(currentCard) && !"LIVE_RESULTS".equals(cardKey)) {
            liveResultsPanel.stopLivePolling();
        }

        currentCard = cardKey;
        cardLayout.show(contentArea, cardKey);

        // Highlight active nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(cardKey);
            JButton b = entry.getValue();
            b.setForeground(active ? Color.WHITE : UIUtils.COLOR_TEXT_SECONDARY);
            b.setOpaque(active);
            b.setBackground(active ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_SIDEBAR);
            b.repaint();
        }

        // Action when entering panel
        if ("DASHBOARD".equals(cardKey)) {
            refreshOverviewStats();
        } else if ("LIVE_RESULTS".equals(cardKey)) {
            liveResultsPanel.startLivePolling();
        } else if ("FINAL_RESULTS".equals(cardKey)) {
            finalResultPanel.loadElections();
        } else if ("ELECTIONS".equals(cardKey)) {
            electionPanel.loadElections();
        } else if ("CANDIDATES".equals(cardKey)) {
            candidatePanel.loadElections();
            candidatePanel.loadCandidates();
        } else if ("VOTERS".equals(cardKey)) {
            voterPanel.loadVoters();
        }
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Greeting
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel welcome = new JLabel("Welcome back, " + (currentAdmin != null ? currentAdmin.getFullName() : "Admin"));
        welcome.setFont(UIUtils.FONT_TITLE);
        welcome.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        activeElectionBanner = new JLabel("Checking active election...");
        activeElectionBanner.setFont(UIUtils.FONT_REGULAR);
        activeElectionBanner.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        topHeader.add(welcome, BorderLayout.NORTH);
        topHeader.add(activeElectionBanner, BorderLayout.SOUTH);
        panel.add(topHeader, BorderLayout.NORTH);

        // Stat Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        cardsGrid.setOpaque(false);
        cardsGrid.setPreferredSize(new Dimension(800, 240));

        totalVotersCard = new StatCard("Total Registered Voters", "0", "Eligible Society Members", UIUtils.COLOR_PRIMARY);
        votesCastCard = new StatCard("Total Votes Cast", "0", "Ballots Recorded", UIUtils.COLOR_SUCCESS);
        votesRemainingCard = new StatCard("Remaining Non-Voters", "0", "Yet to Cast Vote", UIUtils.COLOR_WARNING);
        turnoutCard = new StatCard("Turnout Percentage", "0.0%", "Participation Rate", UIUtils.COLOR_PRIMARY_HOVER);
        electionStatusCard = new StatCard("Active Election Status", "NONE", "Current Cycle", UIUtils.COLOR_SURFACE_LIGHT);
        leadingCandidateCard = new StatCard("Current Leading Candidate", "None", "Rank #1", UIUtils.COLOR_GOLD);

        cardsGrid.add(totalVotersCard);
        cardsGrid.add(votesCastCard);
        cardsGrid.add(votesRemainingCard);
        cardsGrid.add(turnoutCard);
        cardsGrid.add(electionStatusCard);
        cardsGrid.add(leadingCandidateCard);

        // Quick Actions & Instructions Card
        JPanel bottomActionCard = UIUtils.createCardPanel();
        bottomActionCard.setLayout(new BorderLayout(14, 14));

        JLabel actionTitle = new JLabel("QUICK SYSTEM ACTIONS & WORKFLOW GUIDE");
        actionTitle.setFont(UIUtils.FONT_SECTION);
        actionTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        buttonBar.setOpaque(false);

        JButton goToLiveBtn = UIUtils.createPrimaryButton("View Live Realtime Dashboard");
        JButton createElecBtn = UIUtils.createSuccessButton("Manage Elections");
        JButton regVoterBtn = UIUtils.createSecondaryButton("Register Voters");
        JButton viewFinalBtn = UIUtils.createSecondaryButton("View Closed Election Results");

        goToLiveBtn.addActionListener(e -> switchNav("LIVE_RESULTS"));
        createElecBtn.addActionListener(e -> switchNav("ELECTIONS"));
        regVoterBtn.addActionListener(e -> switchNav("VOTERS"));
        viewFinalBtn.addActionListener(e -> switchNav("FINAL_RESULTS"));

        buttonBar.add(goToLiveBtn);
        buttonBar.add(createElecBtn);
        buttonBar.add(regVoterBtn);
        buttonBar.add(viewFinalBtn);

        JTextArea guideText = new JTextArea(
                " Active Poll Monitoring: Click 'View Live Realtime Dashboard' to inspect incoming LAN ballots in real time.\n" +
                " Ending an Election: In 'Elections', select the active poll and click 'Close Poll' to immediately freeze voting and trigger Automatic Winner Declaration.\n" +
                " LAN Client Voting: Voters on any computer on your Wi-Fi/LAN can launch the app, choose 'Voter Login', and submit their ballots."
        );
        guideText.setEditable(false);
        guideText.setFont(UIUtils.FONT_REGULAR);
        guideText.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        guideText.setBackground(UIUtils.COLOR_CARD_BG);
        guideText.setBorder(new EmptyBorder(6, 4, 6, 4));

        bottomActionCard.add(actionTitle, BorderLayout.NORTH);
        bottomActionCard.add(guideText, BorderLayout.CENTER);
        bottomActionCard.add(buttonBar, BorderLayout.SOUTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(16, 16));
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardsGrid, BorderLayout.NORTH);
        centerWrapper.add(bottomActionCard, BorderLayout.CENTER);

        panel.add(centerWrapper, BorderLayout.CENTER);
        return panel;
    }

    private void refreshOverviewStats() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            int totalVoters = 0;
            int votesCast = 0;
            Election active = null;
            String leader = "None";

            @Override
            protected Void doInBackground() throws Exception {
                totalVoters = dashboardService.getTotalRegisteredVoters();
                active = electionService.getActiveElection();
                if (active != null) {
                    votesCast = dashboardService.getTotalVotesCast(active.getElectionId());
                    leader = dashboardService.getLeadingCandidateSummary(active.getElectionId());
                }
                return null;
            }

            @Override
            protected void done() {
                totalVotersCard.setValue(String.valueOf(totalVoters));
                votesCastCard.setValue(String.valueOf(votesCast));
                int rem = Math.max(0, totalVoters - votesCast);
                votesRemainingCard.setValue(String.valueOf(rem));
                double pct = totalVoters > 0 ? ((double) votesCast / totalVoters) * 100.0 : 0.0;
                turnoutCard.setValue(String.format("%.1f%%", pct));

                if (active != null) {
                    electionStatusCard.setValue("ACTIVE");
                    activeElectionBanner.setText("Active Poll: " + active.getElectionName() + " (Closes: " + active.getEndTime() + ")");
                    leadingCandidateCard.setValue(leader.length() > 18 ? leader.split(" ")[0] : leader);
                    leadingCandidateCard.setSubtext(leader);
                } else {
                    electionStatusCard.setValue("INACTIVE");
                    activeElectionBanner.setText("No active election in progress. Go to Elections to start or create one.");
                    leadingCandidateCard.setValue("No Active Poll");
                    leadingCandidateCard.setSubtext("Standby");
                }
            }
        };
        worker.execute();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (liveResultsPanel != null) {
                liveResultsPanel.stopLivePolling();
            }
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
