

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Final Result Screen displaying automatic winner declaration,
 * tie resolutions, offline AI insights, detailed rankings, and CSV export.
 */
public class FinalResultPanel extends JPanel {

    private final ElectionService electionService = new ElectionService();
    private final ResultService resultService = new ResultService();

    private final JComboBox<ElectionItem> electionCombo;
    private final JPanel bannerPanel;
    private final JLabel bannerStatusLabel;
    private final JLabel bannerWinnerNameLabel;
    private final JLabel bannerWinnerDetailsLabel;

    private final StatCard totalVotesCard;
    private final StatCard totalVotersCard;
    private final StatCard turnoutCard;
    private final StatCard runnerUpCard;

    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private final JTextArea aiInsightsArea;

    private ResultService.FinalElectionSummary currentSummary;

    private static class ElectionItem {
        final int id;
        final String name;
        final String status;

        ElectionItem(int id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }

        @Override
        public String toString() {
            return name + " [" + status + "]";
        }
    }

    public FinalResultPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Control Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel title = new JLabel("Official Final Election Results");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JLabel selectLbl = new JLabel("Select Election:");
        selectLbl.setFont(UIUtils.FONT_BOLD);
        selectLbl.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        electionCombo = new JComboBox<>();
        electionCombo.setFont(UIUtils.FONT_REGULAR);
        electionCombo.setPreferredSize(new Dimension(320, 34));
        electionCombo.addActionListener(e -> loadSelectedElectionResults());

        JButton exportCsvBtn = UIUtils.createSuccessButton("Export to CSV");
        exportCsvBtn.addActionListener(e -> exportToCsv());

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadElections());

        rightControls.add(selectLbl);
        rightControls.add(electionCombo);
        rightControls.add(exportCsvBtn);
        rightControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Scrollable Container
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // 1. Winner / Tie Hero Banner
        bannerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isTie = currentSummary != null && currentSummary.isTie();
                GradientPaint gp = isTie ?
                        new GradientPaint(0, 0, new Color(180, 83, 9), getWidth(), getHeight(), new Color(120, 53, 15)) :
                        new GradientPaint(0, 0, new Color(15, 118, 110), getWidth(), getHeight(), new Color(16, 185, 129));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(255, 255, 255, 60));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bannerPanel.setOpaque(false);
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));
        bannerPanel.setMaximumSize(new Dimension(2000, 110));

        bannerStatusLabel = new JLabel("ELECTION CLOSED");
        bannerStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bannerStatusLabel.setForeground(new Color(254, 240, 138));

        bannerWinnerNameLabel = new JLabel("Winner Declaration");
        bannerWinnerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        bannerWinnerNameLabel.setForeground(Color.WHITE);

        bannerWinnerDetailsLabel = new JLabel("Calculating results...");
        bannerWinnerDetailsLabel.setFont(UIUtils.FONT_REGULAR);
        bannerWinnerDetailsLabel.setForeground(new Color(241, 245, 249));

        bannerPanel.add(bannerStatusLabel);
        bannerPanel.add(Box.createVerticalStrut(4));
        bannerPanel.add(bannerWinnerNameLabel);
        bannerPanel.add(Box.createVerticalStrut(4));
        bannerPanel.add(bannerWinnerDetailsLabel);

        centerContent.add(bannerPanel);
        centerContent.add(Box.createVerticalStrut(14));

        // 2. Statistics Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(2000, 105));

        totalVotesCard = new StatCard("Total Votes Cast", "0", "Final Turnout", UIUtils.COLOR_PRIMARY);
        totalVotersCard = new StatCard("Registered Voters", "0", "Eligible Society Members", UIUtils.COLOR_SURFACE_LIGHT);
        turnoutCard = new StatCard("Voting Percentage", "0.0%", "Participation Rate", UIUtils.COLOR_SUCCESS);
        runnerUpCard = new StatCard("Runner Up", "None", "Rank #2 Contender", UIUtils.COLOR_WARNING);

        statsPanel.add(totalVotesCard);
        statsPanel.add(totalVotersCard);
        statsPanel.add(turnoutCard);
        statsPanel.add(runnerUpCard);

        centerContent.add(statsPanel);
        centerContent.add(Box.createVerticalStrut(14));

        // 3. Results Table and AI Insights Panel
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        splitPanel.setOpaque(false);
        splitPanel.setPreferredSize(new Dimension(800, 320));

        // Left: Official Results Table
        JPanel tableContainer = UIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(8, 8));

        JLabel tableTitle = new JLabel("FINAL CANDIDATE RANKINGS");
        tableTitle.setFont(UIUtils.FONT_SECTION);
        tableTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Position", "Candidate", "Symbol", "Votes", "Percentage", "Outcome"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(tableModel);
        UIUtils.styleTable(resultsTable);

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(UIUtils.COLOR_CARD_BG);
        tableContainer.add(tableScroll, BorderLayout.CENTER);
        splitPanel.add(tableContainer);

        // Right: AI Insights Box
        JPanel aiContainer = UIUtils.createCardPanel();
        aiContainer.setLayout(new BorderLayout(8, 8));

        JLabel aiTitle = new JLabel("OFFLINE AI ELECTION INSIGHTS");
        aiTitle.setFont(UIUtils.FONT_SECTION);
        aiTitle.setForeground(UIUtils.COLOR_GOLD);
        aiContainer.add(aiTitle, BorderLayout.NORTH);

        aiInsightsArea = new JTextArea();
        aiInsightsArea.setEditable(false);
        aiInsightsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aiInsightsArea.setBackground(UIUtils.COLOR_CARD_BG);
        aiInsightsArea.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        aiInsightsArea.setLineWrap(true);
        aiInsightsArea.setWrapStyleWord(true);
        aiInsightsArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane aiScroll = new JScrollPane(aiInsightsArea);
        aiScroll.setBorder(BorderFactory.createEmptyBorder());
        aiScroll.getViewport().setBackground(UIUtils.COLOR_CARD_BG);
        aiContainer.add(aiScroll, BorderLayout.CENTER);
        splitPanel.add(aiContainer);

        centerContent.add(splitPanel);

        JScrollPane mainScroll = new JScrollPane(centerContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        add(mainScroll, BorderLayout.CENTER);

        loadElections();
    }

    public void loadElections() {
        try {
            ElectionItem previous = (ElectionItem) electionCombo.getSelectedItem();
            electionCombo.removeAllItems();
            List<Election> list = electionService.getAllElections();
            ElectionItem defaultItem = null;

            for (Election el : list) {
                ElectionItem item = new ElectionItem(el.getElectionId(), el.getElectionName(), el.getStatus());
                electionCombo.addItem(item);
                if ("CLOSED".equalsIgnoreCase(el.getStatus()) && defaultItem == null) {
                    defaultItem = item;
                }
            }

            if (previous != null) {
                for (int i = 0; i < electionCombo.getItemCount(); i++) {
                    if (electionCombo.getItemAt(i).id == previous.id) {
                        electionCombo.setSelectedIndex(i);
                        return;
                    }
                }
            }

            if (defaultItem != null) {
                electionCombo.setSelectedItem(defaultItem);
            } else if (electionCombo.getItemCount() > 0) {
                electionCombo.setSelectedIndex(0);
            }
        } catch (Exception e) {
            System.err.println("[FinalResultPanel] Failed to load elections: " + e.getMessage());
        }
    }

    public void selectElectionById(int electionId) {
        for (int i = 0; i < electionCombo.getItemCount(); i++) {
            if (electionCombo.getItemAt(i).id == electionId) {
                electionCombo.setSelectedIndex(i);
                loadSelectedElectionResults();
                return;
            }
        }
    }

    private void loadSelectedElectionResults() {
        ElectionItem item = (ElectionItem) electionCombo.getSelectedItem();
        if (item == null) return;

        SwingWorker<ResultService.FinalElectionSummary, Void> worker = new SwingWorker<ResultService.FinalElectionSummary, Void>() {
            @Override
            protected ResultService.FinalElectionSummary doInBackground() throws Exception {
                return resultService.getFinalElectionSummary(item.id);
            }

            @Override
            protected void done() {
                try {
                    currentSummary = get();
                    renderSummary(currentSummary);
                } catch (Exception e) {
                    System.err.println("[FinalResultPanel] Error fetching summary: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderSummary(ResultService.FinalElectionSummary summary) {
        if (summary == null || summary.getElection() == null) {
            bannerWinnerNameLabel.setText("No Results Available");
            bannerWinnerDetailsLabel.setText("Election results could not be calculated.");
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        String closeDateStr = summary.getElection().getClosedAt() != null ?
                summary.getElection().getClosedAt().format(dtf) : "In Progress";

        totalVotesCard.setValue(String.valueOf(summary.getTotalVotesCast()));
        totalVotersCard.setValue(String.valueOf(summary.getTotalRegisteredVoters()));
        turnoutCard.setValue(String.format("%.1f%%", summary.getTurnoutPercentage()));

        if (summary.getRunnerUp() != null) {
            runnerUpCard.setValue(summary.getRunnerUp().getCandidateName());
            runnerUpCard.setSubtext(summary.getRunnerUp().getVoteCount() + " votes (" +
                    String.format("%.1f%%", summary.getRunnerUp().getVotePercentage()) + ")");
        } else {
            runnerUpCard.setValue("N/A");
            runnerUpCard.setSubtext("No runner-up");
        }

        // Render Hero Banner
        if (summary.isTie()) {
            bannerStatusLabel.setText("ELECTION STATUS: CLOSED (POLL ENDED)");
            bannerWinnerNameLabel.setText("ELECTION RESULT: TIE");

            if (summary.getTiedWinners() != null && !summary.getTiedWinners().isEmpty()) {
                StringBuilder tieSb = new StringBuilder("Equal Highest Votes (");
                tieSb.append(summary.getTiedWinners().get(0).getVoteCount()).append(" votes each): ");
                for (int i = 0; i < summary.getTiedWinners().size(); i++) {
                    ElectionResult er = summary.getTiedWinners().get(i);
                    tieSb.append(er.getCandidateName()).append(" [").append(er.getSymbol()).append("]");
                    if (i < summary.getTiedWinners().size() - 1) tieSb.append(", ");
                }
                tieSb.append(" | Concluded on ").append(closeDateStr);
                bannerWinnerDetailsLabel.setText(tieSb.toString());
            } else {
                bannerWinnerDetailsLabel.setText("No votes were cast in this election | Closed on " + closeDateStr);
            }
        } else if (summary.getWinner() != null) {
            ElectionResult w = summary.getWinner();
            bannerStatusLabel.setText("ELECTION CLOSED  OFFICIAL WINNER DECLARED");
            bannerWinnerNameLabel.setText(" WINNER: " + w.getCandidateName().toUpperCase() + " [" + w.getSymbol() + "]");
            bannerWinnerDetailsLabel.setText(String.format("Received %d votes (%.1f%% of total) | Runner-Up: %s | Poll Closed: %s",
                    w.getVoteCount(), w.getVotePercentage(),
                    summary.getRunnerUp() != null ? summary.getRunnerUp().getCandidateName() : "None",
                    closeDateStr));
        } else {
            bannerStatusLabel.setText("ELECTION STATUS: " + summary.getElection().getStatus());
            bannerWinnerNameLabel.setText("Poll in Progress");
            bannerWinnerDetailsLabel.setText("Close the poll to automatically declare the winner.");
        }

        bannerPanel.repaint();

        // Render Table
        tableModel.setRowCount(0);
        for (ElectionResult r : summary.getResults()) {
            tableModel.addRow(new Object[]{
                    "#" + r.getPosition(),
                    r.getCandidateName(),
                    r.getSymbol(),
                    r.getVoteCount(),
                    String.format("%.1f%%", r.getVotePercentage()),
                    r.isWinner() ? "WINNER" : (summary.isTie() && r.getPosition() == 1 ? "TIED #1" : "Contender")
            });
        }

        // Render Offline AI Insights
        String insights = AIInsightsService.generateElectionInsights(summary);
        aiInsightsArea.setText(insights);
        aiInsightsArea.setCaretPosition(0);
    }

    private void exportToCsv() {
        if (currentSummary == null || currentSummary.getElection() == null) {
            JOptionPane.showMessageDialog(this, "No election results to export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Election Results CSV");
        String defaultFileName = "Election_Results_" + currentSummary.getElection().getElectionId() + ".csv";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }

            try {
                CSVExporter.exportResultsToCSV(
                        target,
                        currentSummary.getElection(),
                        currentSummary.getResults(),
                        currentSummary.getTotalRegisteredVoters(),
                        currentSummary.getTotalVotesCast(),
                        currentSummary.getStatusMessage()
                );
                JOptionPane.showMessageDialog(this, 
                        "Election results successfully exported to:\n" + target.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                        "Failed to export CSV:\n" + ex.getMessage(), 
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
