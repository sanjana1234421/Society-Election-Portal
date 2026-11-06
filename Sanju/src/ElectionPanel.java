

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Election Management Screen allowing Admin to Create, Edit, Start Poll,
 * and Close Poll with automatic winner declaration.
 */
public class ElectionPanel extends JPanel {

    private final ElectionService electionService = new ElectionService();
    private final ResultService resultService = new ResultService();
    private final Consumer<Integer> onElectionClosedCallback;

    private final JTable electionTable;
    private final DefaultTableModel tableModel;
    private List<Election> currentList;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ElectionPanel(Consumer<Integer> onElectionClosedCallback) {
        this.onElectionClosedCallback = onElectionClosedCallback;
        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header & Action Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel title = new JLabel("Election Management");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton createBtn = UIUtils.createPrimaryButton("+ Create Election");
        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        JButton startPollBtn = UIUtils.createSuccessButton("Start Poll");
        JButton closePollBtn = UIUtils.createDangerButton("Close Poll");
        JButton deleteBtn = UIUtils.createSecondaryButton("Delete");
        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");

        createBtn.addActionListener(e -> showCreateOrEditDialog(null));
        editBtn.addActionListener(e -> editSelectedElection());
        startPollBtn.addActionListener(e -> startSelectedPoll());
        closePollBtn.addActionListener(e -> closeSelectedPoll());
        deleteBtn.addActionListener(e -> deleteSelectedElection());
        refreshBtn.addActionListener(e -> loadElections());

        buttonPanel.add(createBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(startPollBtn);
        buttonPanel.add(closePollBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(buttonPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableContainer = UIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(10, 10));

        String[] columns = {"ID", "Election Name", "Description", "Start Time", "End Time", "Status", "Winner ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        electionTable = new JTable(tableModel);
        UIUtils.styleTable(electionTable);
        electionTable.getColumnModel().getColumn(0).setMaxWidth(60);
        electionTable.getColumnModel().getColumn(5).setMaxWidth(100);
        electionTable.getColumnModel().getColumn(6).setMaxWidth(90);

        JScrollPane scrollPane = new JScrollPane(electionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIUtils.COLOR_CARD_BG);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
        loadElections();
    }

    public void loadElections() {
        SwingWorker<List<Election>, Void> worker = new SwingWorker<List<Election>, Void>() {
            @Override
            protected List<Election> doInBackground() throws Exception {
                return electionService.getAllElections();
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    for (Election el : currentList) {
                        tableModel.addRow(new Object[]{
                                el.getElectionId(),
                                el.getElectionName(),
                                el.getDescription(),
                                el.getStartTime() != null ? el.getStartTime().format(DTF) : "N/A",
                                el.getEndTime() != null ? el.getEndTime().format(DTF) : "N/A",
                                el.getStatus(),
                                el.getWinnerCandidateId() != null ? el.getWinnerCandidateId() : "-"
                        });
                    }
                } catch (Exception e) {
                    System.err.println("[ElectionPanel] Error loading elections: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private Election getSelectedElection() {
        int row = electionTable.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an election from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void editSelectedElection() {
        Election selected = getSelectedElection();
        if (selected != null) {
            showCreateOrEditDialog(selected);
        }
    }

    private void startSelectedPoll() {
        Election selected = getSelectedElection();
        if (selected == null) return;

        if ("ACTIVE".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "This election is already ACTIVE.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("CLOSED".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "A CLOSED election cannot be restarted.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Start poll for '" + selected.getElectionName() + "'?\nResidents will be able to cast their votes immediately.",
                "Confirm Start Poll",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                electionService.startElection(selected.getElectionId());
                JOptionPane.showMessageDialog(this, "Election poll is now ACTIVE and open for voting!", "Poll Started", JOptionPane.INFORMATION_MESSAGE);
                loadElections();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to start poll: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void closeSelectedPoll() {
        Election selected = getSelectedElection();
        if (selected == null) return;

        if ("CLOSED".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "This election has already been closed.", "Information", JOptionPane.INFORMATION_MESSAGE);
            if (onElectionClosedCallback != null) {
                onElectionClosedCallback.accept(selected.getElectionId());
            }
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to close this election? Voting will stop permanently.",
                "Confirm Close Election",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                resultService.closeElectionAndDeclareWinner(selected.getElectionId());
                JOptionPane.showMessageDialog(this, 
                        "Election closed successfully!\nFinal results have been tabulated and winner declared.", 
                        "Election Finalized", JOptionPane.INFORMATION_MESSAGE);
                loadElections();
                if (onElectionClosedCallback != null) {
                    onElectionClosedCallback.accept(selected.getElectionId());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to close election: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedElection() {
        Election selected = getSelectedElection();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + selected.getElectionName() + "' and all associated votes?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                electionService.deleteElection(selected.getElectionId());
                loadElections();
                JOptionPane.showMessageDialog(this, "Election deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete election: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCreateOrEditDialog(Election existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Create New Election" : "Edit Election Details", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIUtils.COLOR_BG_DARK);
        dialog.setLayout(new BorderLayout(15, 15));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(existing != null ? existing.getElectionName() : "");
        JTextArea descArea = new JTextArea(existing != null ? existing.getDescription() : "", 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        UIUtils.styleTextField(nameField);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusDays(2);
        JTextField startField = new JTextField(existing != null && existing.getStartTime() != null ? existing.getStartTime().format(DTF) : now.format(DTF));
        JTextField endField = new JTextField(existing != null && existing.getEndTime() != null ? existing.getEndTime().format(DTF) : later.format(DTF));
        UIUtils.styleTextField(startField);
        UIUtils.styleTextField(endField);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"UPCOMING", "ACTIVE", "CLOSED"});
        if (existing != null) {
            statusCombo.setSelectedItem(existing.getStatus());
        }

        int r = 0;
        addFormRow(form, gbc, r++, "Election Name:", nameField);
        addFormRow(form, gbc, r++, "Description:", new JScrollPane(descArea));
        addFormRow(form, gbc, r++, "Start Date/Time (yyyy-MM-dd HH:mm):", startField);
        addFormRow(form, gbc, r++, "End Date/Time (yyyy-MM-dd HH:mm):", endField);
        addFormRow(form, gbc, r++, "Initial Status:", statusCombo);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(UIUtils.COLOR_SIDEBAR);

        JButton saveBtn = UIUtils.createPrimaryButton(existing == null ? "Create" : "Update");
        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String desc = descArea.getText().trim();
                LocalDateTime start = LocalDateTime.parse(startField.getText().trim(), DTF);
                LocalDateTime end = LocalDateTime.parse(endField.getText().trim(), DTF);
                String status = (String) statusCombo.getSelectedItem();

                if (existing == null) {
                    Election el = new Election(0, name, desc, start, end, status, LocalDateTime.now(), null, null);
                    electionService.createElection(el);
                    JOptionPane.showMessageDialog(dialog, "Election created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    existing.setElectionName(name);
                    existing.setDescription(desc);
                    existing.setStartTime(start);
                    existing.setEndTime(end);
                    existing.setStatus(status);
                    electionService.updateElection(existing);
                    JOptionPane.showMessageDialog(dialog, "Election updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadElections();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_BOLD);
        lbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }
}
