

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Voter Management Screen allowing Admin to register residents, edit profiles,
 * toggle active/inactive status, reset credentials, and search voter lists.
 */
public class VoterPanel extends JPanel {

    private final VoterDAO voterDAO = new VoterDAO();
    private final ElectionDAO electionDAO = new ElectionDAO();
    private final VoteDAO voteDAO = new VoteDAO();

    private final JTextField searchField;
    private final JTable voterTable;
    private final DefaultTableModel tableModel;
    private List<Voter> currentList;

    public VoterPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Control & Search Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel title = new JLabel("Voter Management");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        searchField = new JTextField(14);
        UIUtils.styleTextField(searchField);
        searchField.setToolTipText("Search by Name, Voter ID, or Flat #");

        JButton searchBtn = UIUtils.createSecondaryButton("Search");
        JButton addBtn = UIUtils.createPrimaryButton("+ Add Voter");
        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        JButton toggleStatusBtn = UIUtils.createSecondaryButton("Toggle Status");
        JButton resetPassBtn = UIUtils.createSecondaryButton("Reset Password");
        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");

        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        addBtn.addActionListener(e -> showVoterDialog(null));
        editBtn.addActionListener(e -> editSelectedVoter());
        toggleStatusBtn.addActionListener(e -> toggleSelectedVoterStatus());
        resetPassBtn.addActionListener(e -> resetSelectedVoterPassword());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadVoters();
        });

        rightControls.add(new JLabel("Find:"));
        rightControls.add(searchField);
        rightControls.add(searchBtn);
        rightControls.add(addBtn);
        rightControls.add(editBtn);
        rightControls.add(toggleStatusBtn);
        rightControls.add(resetPassBtn);
        rightControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        JPanel tableContainer = UIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(10, 10));

        String[] columns = {"ID", "Voter Unique ID", "Resident Name", "Flat #", "Phone", "Account Status", "Ballot Status (Active Poll)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        voterTable = new JTable(tableModel);
        UIUtils.styleTable(voterTable);
        voterTable.getColumnModel().getColumn(0).setMaxWidth(60);
        voterTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        voterTable.getColumnModel().getColumn(3).setMaxWidth(90);
        voterTable.getColumnModel().getColumn(5).setMaxWidth(110);

        JScrollPane scrollPane = new JScrollPane(voterTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIUtils.COLOR_CARD_BG);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        loadVoters();
    }

    public void loadVoters() {
        SwingWorker<List<Voter>, Void> worker = new SwingWorker<List<Voter>, Void>() {
            @Override
            protected List<Voter> doInBackground() throws Exception {
                return voterDAO.getAllVoters();
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    populateTable(currentList);
                } catch (Exception e) {
                    System.err.println("[VoterPanel] Error loading voters: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadVoters();
            return;
        }

        SwingWorker<List<Voter>, Void> worker = new SwingWorker<List<Voter>, Void>() {
            @Override
            protected List<Voter> doInBackground() throws Exception {
                return voterDAO.searchVoters(query);
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    populateTable(currentList);
                } catch (Exception e) {
                    System.err.println("[VoterPanel] Search error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Voter> voters) {
        tableModel.setRowCount(0);
        // Find if an active election exists to show ballot status
        Integer activeElectionId = null;
        try {
            Election active = electionDAO.getActiveElection();
            if (active != null) activeElectionId = active.getElectionId();
        } catch (Exception ignored) {}

        for (Voter v : voters) {
            String ballotStatus = "No Active Poll";
            if (activeElectionId != null) {
                try {
                    boolean voted = voteDAO.hasVoterVoted(activeElectionId, v.getVoterId());
                    ballotStatus = voted ? "VOTED" : "PENDING";
                } catch (Exception ignored) {}
            }

            tableModel.addRow(new Object[]{
                    v.getVoterId(),
                    v.getVoterUniqueId(),
                    v.getName(),
                    v.getFlatNumber(),
                    v.getPhone(),
                    v.getStatus(),
                    ballotStatus
            });
        }
    }

    private Voter getSelectedVoter() {
        int row = voterTable.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a voter from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void editSelectedVoter() {
        Voter v = getSelectedVoter();
        if (v != null) {
            showVoterDialog(v);
        }
    }

    private void toggleSelectedVoterStatus() {
        Voter v = getSelectedVoter();
        if (v == null) return;

        String newStatus = "ACTIVE".equalsIgnoreCase(v.getStatus()) ? "INACTIVE" : "ACTIVE";
        try {
            voterDAO.updateStatus(v.getVoterId(), newStatus);
            v.setStatus(newStatus);
            loadVoters();
            JOptionPane.showMessageDialog(this, "Voter status updated to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetSelectedVoterPassword() {
        Voter v = getSelectedVoter();
        if (v == null) return;

        String newPin = JOptionPane.showInputDialog(this, 
                "Enter new 4-digit PIN/Password for " + v.getName() + " (" + v.getVoterUniqueId() + "):", 
                "Reset Password", JOptionPane.PLAIN_MESSAGE);

        if (newPin != null && !newPin.trim().isEmpty()) {
            if (!ValidationUtil.isValidPassword(newPin)) {
                JOptionPane.showMessageDialog(this, "PIN/Password must be at least 4 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String hashed = PasswordUtil.hashPassword(newPin.trim());
                voterDAO.resetPassword(v.getVoterId(), hashed);
                JOptionPane.showMessageDialog(this, "Password/PIN reset successfully for " + v.getName() + ".", "Password Reset", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to reset password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showVoterDialog(Voter existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Register New Society Voter" : "Edit Voter Details", true);
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

        JTextField idField = new JTextField(existing != null ? existing.getVoterUniqueId() : "");
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        JTextField flatField = new JTextField(existing != null ? existing.getFlatNumber() : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "");
        JPasswordField passField = new JPasswordField(existing != null ? "" : "1234");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        if (existing != null) {
            statusCombo.setSelectedItem(existing.getStatus());
            idField.setEnabled(false); // Unique ID cannot be changed once created
        }

        UIUtils.styleTextField(idField);
        UIUtils.styleTextField(nameField);
        UIUtils.styleTextField(flatField);
        UIUtils.styleTextField(phoneField);
        UIUtils.styleTextField(passField);

        int r = 0;
        addFormRow(form, gbc, r++, "Voter Unique ID:", idField);
        addFormRow(form, gbc, r++, "Resident Name:", nameField);
        addFormRow(form, gbc, r++, "Flat Number:", flatField);
        addFormRow(form, gbc, r++, "Phone Number:", phoneField);
        if (existing == null) {
            addFormRow(form, gbc, r++, "Initial PIN/Password:", passField);
        }
        addFormRow(form, gbc, r++, "Status:", statusCombo);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(UIUtils.COLOR_SIDEBAR);

        JButton saveBtn = UIUtils.createPrimaryButton(existing == null ? "Register" : "Update");
        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");

        saveBtn.addActionListener(e -> {
            String uid = idField.getText().trim();
            String name = nameField.getText().trim();
            String flat = flatField.getText().trim();
            String phone = phoneField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            if (existing == null && !ValidationUtil.isValidVoterId(uid)) {
                JOptionPane.showMessageDialog(dialog, "Voter ID must be alphanumeric and 3-50 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidName(name)) {
                JOptionPane.showMessageDialog(dialog, "Name must be 2-100 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidFlatNumber(flat)) {
                JOptionPane.showMessageDialog(dialog, "Flat number cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Phone must contain 7-15 valid digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (existing == null) {
                    String plainPass = new String(passField.getPassword()).trim();
                    if (!ValidationUtil.isValidPassword(plainPass)) {
                        JOptionPane.showMessageDialog(dialog, "Password must be at least 4 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String hashed = PasswordUtil.hashPassword(plainPass);
                    Voter newVoter = new Voter(0, uid, name, phone, flat, hashed, status, null);
                    voterDAO.createVoter(newVoter);
                    JOptionPane.showMessageDialog(dialog, "Voter registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    existing.setName(name);
                    existing.setFlatNumber(flat);
                    existing.setPhone(phone);
                    existing.setStatus(status);
                    voterDAO.updateVoter(existing);
                    JOptionPane.showMessageDialog(dialog, "Voter updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadVoters();
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
