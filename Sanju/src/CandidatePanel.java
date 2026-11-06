

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Candidate Management Screen allowing Admin to add, edit, and delete candidates
 * with symbol assignment and vote count display.
 */
public class CandidatePanel extends JPanel {

    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final ElectionService electionService = new ElectionService();

    private final JComboBox<ElectionItem> electionCombo;
    private final JTable candidateTable;
    private final DefaultTableModel tableModel;
    private List<Candidate> currentList;

    private static final String[] PRESET_SYMBOLS = {
            "Torch", "Tree", "Book", "Sun", "Star", "Scale", "Bell", "Clock", "Eagle", "Key", "Lamp", "Bicycle"
    };

    private static class ElectionItem {
        final int id;
        final String name;

        ElectionItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    public CandidatePanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header & Actions
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel title = new JLabel("Candidate Management");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JLabel filterLbl = new JLabel("Election:");
        filterLbl.setFont(UIUtils.FONT_BOLD);
        filterLbl.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        electionCombo = new JComboBox<>();
        electionCombo.setFont(UIUtils.FONT_REGULAR);
        electionCombo.setPreferredSize(new Dimension(280, 34));
        electionCombo.addActionListener(e -> loadCandidates());

        JButton addBtn = UIUtils.createPrimaryButton("+ Add Candidate");
        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        JButton deleteBtn = UIUtils.createDangerButton("Delete");
        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");

        addBtn.addActionListener(e -> showCandidateDialog(null));
        editBtn.addActionListener(e -> editSelectedCandidate());
        deleteBtn.addActionListener(e -> deleteSelectedCandidate());
        refreshBtn.addActionListener(e -> loadElections());

        rightControls.add(filterLbl);
        rightControls.add(electionCombo);
        rightControls.add(addBtn);
        rightControls.add(editBtn);
        rightControls.add(deleteBtn);
        rightControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableContainer = UIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(10, 10));

        String[] columns = {"Candidate ID", "Name", "Symbol", "Description / Manifesto", "Status", "Current Votes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        candidateTable = new JTable(tableModel);
        UIUtils.styleTable(candidateTable);
        candidateTable.getColumnModel().getColumn(0).setMaxWidth(90);
        candidateTable.getColumnModel().getColumn(2).setMaxWidth(100);
        candidateTable.getColumnModel().getColumn(4).setMaxWidth(100);
        candidateTable.getColumnModel().getColumn(5).setMaxWidth(110);

        JScrollPane scrollPane = new JScrollPane(candidateTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIUtils.COLOR_CARD_BG);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        loadElections();
    }

    public void loadElections() {
        try {
            ElectionItem prev = (ElectionItem) electionCombo.getSelectedItem();
            electionCombo.removeAllItems();
            List<Election> elections = electionService.getAllElections();
            for (Election el : elections) {
                electionCombo.addItem(new ElectionItem(el.getElectionId(), el.getElectionName()));
            }

            if (prev != null) {
                for (int i = 0; i < electionCombo.getItemCount(); i++) {
                    if (electionCombo.getItemAt(i).id == prev.id) {
                        electionCombo.setSelectedIndex(i);
                        return;
                    }
                }
            }
            if (electionCombo.getItemCount() > 0) {
                electionCombo.setSelectedIndex(0);
            }
        } catch (Exception e) {
            System.err.println("[CandidatePanel] Error loading elections: " + e.getMessage());
        }
    }

    public void loadCandidates() {
        ElectionItem selected = (ElectionItem) electionCombo.getSelectedItem();
        if (selected == null) {
            tableModel.setRowCount(0);
            return;
        }

        SwingWorker<List<Candidate>, Void> worker = new SwingWorker<List<Candidate>, Void>() {
            @Override
            protected List<Candidate> doInBackground() throws Exception {
                return candidateDAO.getCandidatesByElection(selected.id);
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    for (Candidate c : currentList) {
                        tableModel.addRow(new Object[]{
                                c.getCandidateId(),
                                c.getCandidateName(),
                                c.getSymbol(),
                                c.getDescription(),
                                c.getStatus(),
                                c.getVoteCount()
                        });
                    }
                } catch (Exception e) {
                    System.err.println("[CandidatePanel] Error loading candidates: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private Candidate getSelectedCandidate() {
        int row = candidateTable.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a candidate from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void editSelectedCandidate() {
        Candidate c = getSelectedCandidate();
        if (c != null) {
            showCandidateDialog(c);
        }
    }

    private void deleteSelectedCandidate() {
        Candidate c = getSelectedCandidate();
        if (c == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete candidate '" + c.getCandidateName() + "'?\nThis will remove any votes cast for this candidate.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                candidateDAO.deleteCandidate(c.getCandidateId());
                loadCandidates();
                JOptionPane.showMessageDialog(this, "Candidate deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete candidate: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCandidateDialog(Candidate existing) {
        ElectionItem selectedElection = (ElectionItem) electionCombo.getSelectedItem();
        if (selectedElection == null) {
            JOptionPane.showMessageDialog(this, "Please select or create an election first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add New Candidate" : "Edit Candidate", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIUtils.COLOR_BG_DARK);
        dialog.setLayout(new BorderLayout(15, 15));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(existing != null ? existing.getCandidateName() : "");
        UIUtils.styleTextField(nameField);

        JComboBox<String> symbolCombo = new JComboBox<>(PRESET_SYMBOLS);
        symbolCombo.setEditable(true);
        if (existing != null) {
            symbolCombo.setSelectedItem(existing.getSymbol());
        }

        JTextArea descArea = new JTextArea(existing != null ? existing.getDescription() : "", 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        if (existing != null) {
            statusCombo.setSelectedItem(existing.getStatus());
        }

        int r = 0;
        addFormRow(form, gbc, r++, "Candidate Name:", nameField);
        addFormRow(form, gbc, r++, "Candidate Symbol:", symbolCombo);
        addFormRow(form, gbc, r++, "Manifesto / Description:", new JScrollPane(descArea));
        addFormRow(form, gbc, r++, "Status:", statusCombo);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(UIUtils.COLOR_SIDEBAR);

        JButton saveBtn = UIUtils.createPrimaryButton(existing == null ? "Save" : "Update");
        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String symbol = symbolCombo.getSelectedItem() != null ? symbolCombo.getSelectedItem().toString().trim() : "";
            String desc = descArea.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            if (!ValidationUtil.isValidName(name)) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid candidate name (2-100 characters).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (symbol.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please choose or enter a candidate symbol.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (existing == null) {
                    Candidate c = new Candidate(0, selectedElection.id, name, symbol, desc, status);
                    candidateDAO.createCandidate(c);
                    JOptionPane.showMessageDialog(dialog, "Candidate added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    existing.setCandidateName(name);
                    existing.setSymbol(symbol);
                    existing.setDescription(desc);
                    existing.setStatus(status);
                    candidateDAO.updateCandidate(existing);
                    JOptionPane.showMessageDialog(dialog, "Candidate updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadCandidates();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
