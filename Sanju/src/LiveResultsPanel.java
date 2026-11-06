

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Real-Time Vote Count Dashboard.
 * Automatically polls database every 1.5 seconds in the background
 * to render live vote tallies, leaderboards, and graphical bar charts.
 */
public class LiveResultsPanel extends JPanel {

    private final DashboardService dashboardService = new DashboardService();
    private final ElectionService electionService = new ElectionService();

    private final JComboBox<ElectionItem> electionCombo;
    private final JLabel livePulseLabel;

    private final StatCard totalVotersCard;
    private final StatCard votesCastCard;
    private final StatCard votesRemainingCard;
    private final StatCard turnoutCard;
    private final StatCard leadingCandidateCard;

    private final VoteBarChartPanel barChartPanel;
    private final JTable leaderboardTable;
    private final DefaultTableModel tableModel;

    private LiveDashboardUpdater updater;

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

    public LiveResultsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Control Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitle.setOpaque(false);

        JLabel title = new JLabel("Realtime Live Vote Dashboard");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        livePulseLabel = new JLabel(" LIVE REFRESHING (1.5s)");
        livePulseLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        livePulseLabel.setForeground(UIUtils.COLOR_SUCCESS);

        leftTitle.add(title);
        leftTitle.add(livePulseLabel);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JLabel selectLbl = new JLabel("Select Election:");
        selectLbl.setFont(UIUtils.FONT_BOLD);
        selectLbl.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        electionCombo = new JComboBox<>();
        electionCombo.setFont(UIUtils.FONT_REGULAR);
        electionCombo.setPreferredSize(new Dimension(320, 34));
        electionCombo.addActionListener(e -> {
            ElectionItem item = (ElectionItem) electionCombo.getSelectedItem();
            if (item != null) {
                currentSelectedElectionId.set(item.id);
            }
            refreshDataSync();
        });

        JButton refreshNowBtn = UIUtils.createSecondaryButton("Refresh Now");
        refreshNowBtn.addActionListener(e -> refreshDataSync());

        rightControls.add(selectLbl);
        rightControls.add(electionCombo);
        rightControls.add(refreshNowBtn);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Stats Cards Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(800, 105));

        totalVotersCard = new StatCard("Total Voters", "0", "Eligible Society Members", UIUtils.COLOR_PRIMARY);
        votesCastCard = new StatCard("Votes Cast", "0", "Ballots Submitted", UIUtils.COLOR_SUCCESS);
        votesRemainingCard = new StatCard("Votes Remaining", "0", "Yet to Vote", UIUtils.COLOR_WARNING);
        turnoutCard = new StatCard("Turnout %", "0.0%", "Participation Rate", UIUtils.COLOR_PRIMARY_HOVER);
        leadingCandidateCard = new StatCard("Leading Candidate", "None", "Current Rank #1", UIUtils.COLOR_GOLD);

        statsPanel.add(totalVotersCard);
        statsPanel.add(votesCastCard);
        statsPanel.add(votesRemainingCard);
        statsPanel.add(turnoutCard);
        statsPanel.add(leadingCandidateCard);

        // Center Content Split: Chart on Left, Leaderboard Table on Right
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centerPanel.setOpaque(false);

        // Bar Chart
        barChartPanel = new VoteBarChartPanel();
        centerPanel.add(barChartPanel);

        // Leaderboard Table
        JPanel tableContainer = UIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(8, 8));

        JLabel tableTitle = new JLabel("LEADERBOARD STANDINGS");
        tableTitle.setFont(UIUtils.FONT_SECTION);
        tableTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Rank", "Candidate Name", "Symbol", "Votes", "Vote %", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        leaderboardTable = new JTable(tableModel);
        UIUtils.styleTable(leaderboardTable);

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIUtils.COLOR_CARD_BG);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer);

        // Layout Integration
        JPanel mainContent = new JPanel(new BorderLayout(14, 14));
        mainContent.setOpaque(false);
        mainContent.add(statsPanel, BorderLayout.NORTH);
        mainContent.add(centerPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        loadElections();
        initLiveUpdater();
    }

    public void loadElections() {
        try {
            ElectionItem previousSelected = (ElectionItem) electionCombo.getSelectedItem();
            electionCombo.removeAllItems();
            List<Election> list = electionService.getAllElections();
            ElectionItem activeItem = null;

            for (Election el : list) {
                ElectionItem item = new ElectionItem(el.getElectionId(), el.getElectionName(), el.getStatus());
                electionCombo.addItem(item);
                if ("ACTIVE".equalsIgnoreCase(el.getStatus()) && activeItem == null) {
                    activeItem = item;
                }
            }

            if (previousSelected != null) {
                for (int i = 0; i < electionCombo.getItemCount(); i++) {
                    if (electionCombo.getItemAt(i).id == previousSelected.id) {
                        electionCombo.setSelectedIndex(i);
                        currentSelectedElectionId.set(previousSelected.id);
                        return;
                    }
                }
            }

            if (activeItem != null) {
                electionCombo.setSelectedItem(activeItem);
                currentSelectedElectionId.set(activeItem.id);
            } else if (electionCombo.getItemCount() > 0) {
                ElectionItem first = electionCombo.getItemAt(0);
                electionCombo.setSelectedIndex(0);
                currentSelectedElectionId.set(first.id);
            }
        } catch (Exception e) {
            System.err.println("[LiveResultsPanel] Failed to load elections: " + e.getMessage());
        }
    }

    private void initLiveUpdater() {
        updater = new LiveDashboardUpdater(1500, this::fetchAndRenderLiveStats);
    }

    public void startLivePolling() {
        loadElections();
        if (updater != null) {
            updater.start();
        }
    }

    public void stopLivePolling() {
        if (updater != null) {
            updater.stop();
        }
    }

    public void refreshDataSync() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                fetchAndRenderLiveStats();
                return null;
            }
        };
        worker.execute();
    }

    private final java.util.concurrent.atomic.AtomicInteger currentSelectedElectionId = new java.util.concurrent.atomic.AtomicInteger(-1);

    private void fetchAndRenderLiveStats() {
        int electionId = currentSelectedElectionId.get();
        if (electionId <= 0) {
            ElectionItem selected = (ElectionItem) electionCombo.getSelectedItem();
            if (selected != null) {
                electionId = selected.id;
                currentSelectedElectionId.set(electionId);
            } else {
                return;
            }
        }
        try {
            int totalVoters = dashboardService.getTotalRegisteredVoters();
            int votesCast = dashboardService.getTotalVotesCast(electionId);
            int remaining = Math.max(0, totalVoters - votesCast);
            double turnout = totalVoters > 0 ? ((double) votesCast / totalVoters) * 100.0 : 0.0;
            String leaderSummary = dashboardService.getLeadingCandidateSummary(electionId);
            List<LiveVoteStat> stats = dashboardService.getLiveVoteStats(electionId);

            SwingUtilities.invokeLater(() -> {
                totalVotersCard.setValue(String.valueOf(totalVoters));
                votesCastCard.setValue(String.valueOf(votesCast));
                votesRemainingCard.setValue(String.valueOf(remaining));
                turnoutCard.setValue(String.format("%.1f%%", turnout));
                leadingCandidateCard.setValue(leaderSummary);
                if (leaderSummary.length() > 20) {
                    leadingCandidateCard.setSubtext(leaderSummary);
                    leadingCandidateCard.setValue(leaderSummary.split(" ")[0]);
                } else {
                    leadingCandidateCard.setSubtext("Rank #1 Standings");
                }

                barChartPanel.updateData(stats, votesCast);

                // Update Table
                tableModel.setRowCount(0);
                for (LiveVoteStat s : stats) {
                    tableModel.addRow(new Object[]{
                            s.getRank(),
                            s.getCandidateName(),
                            s.getSymbol(),
                            s.getVoteCount(),
                            String.format("%.1f%%", s.getVotePercentage()),
                            s.isLeading() && s.getVoteCount() > 0 ? " LEADING" : "Contender"
                    });
                }
            });
        } catch (Exception ex) {
            System.err.println("[LiveResultsPanel] Poll error: " + ex.getMessage());
        }
    }
}
