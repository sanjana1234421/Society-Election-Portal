

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.List;

/**
 * Modern, distraction-free Voter Voting Screen.
 * Allows a resident to view active ballot, select exactly one candidate,
 * review their choice, and submit their vote with instant confirmation.
 */
public class VoterDashboard extends JFrame {

    private final Voter currentVoter;
    private final ElectionService electionService = new ElectionService();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final VotingService votingService = new VotingService();

    private Election activeElection;
    private boolean hasAlreadyVoted = false;

    private final JPanel centerContainer;
    private final ButtonGroup candidateButtonGroup = new ButtonGroup();
    private JButton submitVoteBtn;
    private JLabel ballotStatusLabel;

    public VoterDashboard(Voter voter) {
        super("Society Official Ballot - Voter Terminal");
        this.currentVoter = voter;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.COLOR_BG_DARK);
        setLayout(new BorderLayout(14, 14));

        // 1. Top Header Bar
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Voting Area
        centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setBackground(UIUtils.COLOR_BG_DARK);
        centerContainer.setBorder(new EmptyBorder(10, 24, 10, 24));

        JScrollPane scrollPane = new JScrollPane(centerContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel footerBar = createFooterBar();
        add(footerBar, BorderLayout.SOUTH);

        loadBallotData();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_SIDEBAR);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel appTitle = new JLabel("Official Resident Ballot");
        appTitle.setFont(UIUtils.FONT_TITLE);
        appTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel societyLabel = new JLabel("Green Valley Residential Society  Offline LAN Voting Station");
        societyLabel.setFont(UIUtils.FONT_SMALL);
        societyLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        left.add(appTitle);
        left.add(Box.createVerticalStrut(2));
        left.add(societyLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        right.setOpaque(false);

        JPanel voterInfo = new JPanel();
        voterInfo.setLayout(new BoxLayout(voterInfo, BoxLayout.Y_AXIS));
        voterInfo.setOpaque(false);

        JLabel nameLbl = new JLabel(currentVoter.getName() + " (Flat " + currentVoter.getFlatNumber() + ")");
        nameLbl.setFont(UIUtils.FONT_BOLD);
        nameLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel idLbl = new JLabel("Voter ID: " + currentVoter.getVoterUniqueId());
        idLbl.setFont(UIUtils.FONT_SMALL);
        idLbl.setForeground(UIUtils.COLOR_PRIMARY);

        voterInfo.add(nameLbl);
        voterInfo.add(idLbl);

        JButton logoutBtn = UIUtils.createSecondaryButton("Logout");
        logoutBtn.setFont(UIUtils.FONT_SMALL);
        logoutBtn.addActionListener(e -> logout());

        right.add(voterInfo);
        right.add(logoutBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIUtils.COLOR_SIDEBAR);
        footer.setBorder(new EmptyBorder(12, 24, 12, 24));

        ballotStatusLabel = new JLabel("Please select one candidate to cast your vote.");
        ballotStatusLabel.setFont(UIUtils.FONT_REGULAR);
        ballotStatusLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        submitVoteBtn = UIUtils.createPrimaryButton("Confirm & Submit Vote");
        submitVoteBtn.setFont(UIUtils.FONT_SECTION);
        submitVoteBtn.setPreferredSize(new Dimension(240, 42));
        submitVoteBtn.addActionListener(e -> handleSubmitVote());

        footer.add(ballotStatusLabel, BorderLayout.WEST);
        footer.add(submitVoteBtn, BorderLayout.EAST);
        return footer;
    }

    private void loadBallotData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<Candidate> candidateList;

            @Override
            protected Void doInBackground() throws Exception {
                activeElection = electionService.getActiveElection();
                if (activeElection != null) {
                    hasAlreadyVoted = votingService.hasVoterVoted(activeElection.getElectionId(), currentVoter.getVoterId());
                    candidateList = candidateDAO.getActiveCandidatesByElection(activeElection.getElectionId());
                }
                return null;
            }

            @Override
            protected void done() {
                renderBallot(candidateList);
            }
        };
        worker.execute();
    }

    private void renderBallot(List<Candidate> candidates) {
        centerContainer.removeAll();
        candidateButtonGroup.clearSelection();

        if (activeElection == null) {
            submitVoteBtn.setEnabled(false);
            ballotStatusLabel.setText("No active election in progress.");

            JPanel noElectionCard = UIUtils.createCardPanel();
            noElectionCard.setLayout(new BoxLayout(noElectionCard, BoxLayout.Y_AXIS));
            noElectionCard.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel title = new JLabel("No Active Election");
            title.setFont(UIUtils.FONT_SUBTITLE);
            title.setForeground(UIUtils.COLOR_WARNING);

            JLabel msg = new JLabel("There is currently no ongoing election open for voting. Please check back later.");
            msg.setFont(UIUtils.FONT_REGULAR);
            msg.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

            noElectionCard.add(title);
            noElectionCard.add(Box.createVerticalStrut(6));
            noElectionCard.add(msg);
            centerContainer.add(noElectionCard);
            centerContainer.revalidate();
            centerContainer.repaint();
            return;
        }

        // Active Election Banner Card
        JPanel electionCard = UIUtils.createCardPanel();
        electionCard.setLayout(new BorderLayout(10, 8));
        electionCard.setMaximumSize(new Dimension(2000, 100));

        JPanel elLeft = new JPanel();
        elLeft.setLayout(new BoxLayout(elLeft, BoxLayout.Y_AXIS));
        elLeft.setOpaque(false);

        JLabel elTitle = new JLabel(activeElection.getElectionName());
        elTitle.setFont(UIUtils.FONT_SUBTITLE);
        elTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel elDesc = new JLabel(activeElection.getDescription());
        elDesc.setFont(UIUtils.FONT_REGULAR);
        elDesc.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        JLabel elDate = new JLabel("Voting Closes: " + (activeElection.getEndTime() != null ? activeElection.getEndTime().format(dtf) : "N/A"));
        elDate.setFont(UIUtils.FONT_SMALL);
        elDate.setForeground(UIUtils.COLOR_PRIMARY);

        elLeft.add(elTitle);
        elLeft.add(Box.createVerticalStrut(4));
        elLeft.add(elDesc);
        elLeft.add(Box.createVerticalStrut(4));
        elLeft.add(elDate);

        StatusBadge statusBadge = new StatusBadge(activeElection.getStatus());
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(statusBadge);

        electionCard.add(elLeft, BorderLayout.CENTER);
        electionCard.add(badgeWrapper, BorderLayout.EAST);
        centerContainer.add(electionCard);
        centerContainer.add(Box.createVerticalStrut(16));

        // Check if voter has already voted
        if (hasAlreadyVoted) {
            submitVoteBtn.setEnabled(false);
            ballotStatusLabel.setText("Ballot already cast.");

            JPanel alreadyVotedCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(16, 185, 129, 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(UIUtils.COLOR_SUCCESS);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            alreadyVotedCard.setOpaque(false);
            alreadyVotedCard.setLayout(new BoxLayout(alreadyVotedCard, BoxLayout.Y_AXIS));
            alreadyVotedCard.setBorder(new EmptyBorder(24, 24, 24, 24));
            alreadyVotedCard.setMaximumSize(new Dimension(2000, 120));

            JLabel checkIcon = new JLabel(" VOTE ALREADY RECORDED");
            checkIcon.setFont(UIUtils.FONT_SUBTITLE);
            checkIcon.setForeground(UIUtils.COLOR_SUCCESS);

            JLabel msg = new JLabel("You have already cast your ballot in this election. According to election rules, double-voting is strictly prohibited.");
            msg.setFont(UIUtils.FONT_REGULAR);
            msg.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

            JLabel subMsg = new JLabel("Thank you for exercising your democratic right as a responsible society resident.");
            subMsg.setFont(UIUtils.FONT_SMALL);
            subMsg.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

            alreadyVotedCard.add(checkIcon);
            alreadyVotedCard.add(Box.createVerticalStrut(6));
            alreadyVotedCard.add(msg);
            alreadyVotedCard.add(Box.createVerticalStrut(4));
            alreadyVotedCard.add(subMsg);

            centerContainer.add(alreadyVotedCard);
            centerContainer.revalidate();
            centerContainer.repaint();
            return;
        }

        // Candidate Selection Cards
        JLabel selectPrompt = new JLabel("Select ONE candidate from the ballot below:");
        selectPrompt.setFont(UIUtils.FONT_SECTION);
        selectPrompt.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        centerContainer.add(selectPrompt);
        centerContainer.add(Box.createVerticalStrut(10));

        if (candidates == null || candidates.isEmpty()) {
            JLabel noCand = new JLabel("No registered candidates found for this election.");
            noCand.setFont(UIUtils.FONT_REGULAR);
            noCand.setForeground(UIUtils.COLOR_TEXT_MUTED);
            centerContainer.add(noCand);
            submitVoteBtn.setEnabled(false);
        } else {
            for (Candidate c : candidates) {
                JPanel candCard = createCandidateCard(c);
                centerContainer.add(candCard);
                centerContainer.add(Box.createVerticalStrut(10));
            }
            submitVoteBtn.setEnabled(true);
        }

        centerContainer.revalidate();
        centerContainer.repaint();
    }

    private JPanel createCandidateCard(Candidate candidate) {
        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout(14, 10));
        card.setMaximumSize(new Dimension(2000, 95));

        // Radio Button
        JRadioButton radioButton = new JRadioButton();
        radioButton.setOpaque(false);
        radioButton.setActionCommand(String.valueOf(candidate.getCandidateId()));
        radioButton.putClientProperty("candidate", candidate);
        candidateButtonGroup.add(radioButton);

        // Candidate details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(candidate.getCandidateName());
        nameLabel.setFont(UIUtils.FONT_SUBTITLE);
        nameLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel descLabel = new JLabel(candidate.getDescription() != null && !candidate.getDescription().isEmpty() ?
                candidate.getDescription() : "No manifesto provided.");
        descLabel.setFont(UIUtils.FONT_REGULAR);
        descLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(descLabel);

        // Symbol Badge
        JPanel symbolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        symbolPanel.setOpaque(false);

        JLabel symbolTag = new JLabel("Symbol: " + candidate.getSymbol());
        symbolTag.setFont(UIUtils.FONT_BOLD);
        symbolTag.setForeground(UIUtils.COLOR_PRIMARY);
        symbolTag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_PRIMARY, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        symbolPanel.add(symbolTag);

        card.add(radioButton, BorderLayout.WEST);
        card.add(detailsPanel, BorderLayout.CENTER);
        card.add(symbolPanel, BorderLayout.EAST);

        // Clicking anywhere on the card selects the radio button
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                radioButton.setSelected(true);
            }
        });

        return card;
    }

    private void handleSubmitVote() {
        ButtonModel selectedModel = candidateButtonGroup.getSelection();
        if (selectedModel == null) {
            JOptionPane.showMessageDialog(this, "Please select a candidate before submitting your vote.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int candidateId = Integer.parseInt(selectedModel.getActionCommand());
        Candidate chosenCandidate = null;

        for (Enumeration<AbstractButton> buttons = candidateButtonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton b = buttons.nextElement();
            if (b.getModel() == selectedModel) {
                chosenCandidate = (Candidate) b.getClientProperty("candidate");
                break;
            }
        }

        String chosenName = chosenCandidate != null ? chosenCandidate.getCandidateName() + " (" + chosenCandidate.getSymbol() + ")" : "Candidate #" + candidateId;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to vote for:\n\n" + chosenName + "\n\nNote: You CANNOT change your vote after confirmation.",
                "Confirm Ballot Submission",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            submitVoteBtn.setEnabled(false);
            submitVoteBtn.setText("Submitting...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    votingService.submitVote(activeElection.getElectionId(), currentVoter.getVoterId(), candidateId);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(VoterDashboard.this,
                                "Vote submitted successfully!\nThank you for casting your vote in the " + activeElection.getElectionName() + ".",
                                "Vote Confirmation", JOptionPane.INFORMATION_MESSAGE);
                        hasAlreadyVoted = true;
                        loadBallotData();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(VoterDashboard.this,
                                "Vote Submission Failed:\n" + cause.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        submitVoteBtn.setEnabled(true);
                        submitVoteBtn.setText("Confirm & Submit Vote");
                    }
                }
            };
            worker.execute();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout from the voting terminal?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
