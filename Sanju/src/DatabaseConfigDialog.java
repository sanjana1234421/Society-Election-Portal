

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Modal dialog for configuring LAN MySQL connection parameters.
 */
public class DatabaseConfigDialog extends JDialog {

    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField dbNameField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel statusLabel;

    public DatabaseConfigDialog(Frame owner) {
        super(owner, "Database & LAN Connection Configuration", true);
        setSize(480, 440);
        setLocationRelativeTo(owner);
        setResizable(false);
        getContentPane().setBackground(UIUtils.COLOR_BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBackground(UIUtils.COLOR_SIDEBAR);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("LAN Database Settings");
        titleLabel.setFont(UIUtils.FONT_SUBTITLE);
        titleLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Configure MySQL Server IP for offline LAN operation");
        subLabel.setFont(UIUtils.FONT_SMALL);
        subLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        hostField = new JTextField(DBConnection.getDbHost());
        portField = new JTextField(String.valueOf(DBConnection.getDbPort()));
        dbNameField = new JTextField(DBConnection.getDbName());
        userField = new JTextField(DBConnection.getDbUsername());
        passField = new JPasswordField(DBConnection.getDbPassword());

        UIUtils.styleTextField(hostField);
        UIUtils.styleTextField(portField);
        UIUtils.styleTextField(dbNameField);
        UIUtils.styleTextField(userField);
        UIUtils.styleTextField(passField);

        int row = 0;
        addFormField(formPanel, gbc, row++, "MySQL Server Host / LAN IP:", hostField, "e.g. 192.168.1.100 or localhost");
        addFormField(formPanel, gbc, row++, "Port:", portField, "Default: 3306");
        addFormField(formPanel, gbc, row++, "Database Name:", dbNameField, "Default: society_voting");
        addFormField(formPanel, gbc, row++, "Username:", userField, "Default: root");
        addFormField(formPanel, gbc, row++, "Password:", passField, "Database root/user password");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIUtils.FONT_SMALL);
        statusLabel.setForeground(UIUtils.COLOR_WARNING);
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttonPanel.setBackground(UIUtils.COLOR_SIDEBAR);

        JButton testBtn = UIUtils.createSecondaryButton("Test Connection");
        JButton initBtn = UIUtils.createButton("Init Schema", new Color(139, 92, 246), new Color(124, 58, 237), Color.WHITE);
        JButton saveBtn = UIUtils.createPrimaryButton("Save & Apply");
        JButton closeBtn = UIUtils.createSecondaryButton("Close");

        testBtn.addActionListener(e -> testConnection());
        initBtn.addActionListener(e -> initSchema());
        saveBtn.addActionListener(e -> saveSettings());
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(initBtn);
        buttonPanel.add(testBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field, String hint) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIUtils.FONT_BOLD);
        lbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void testConnection() {
        statusLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        statusLabel.setText("Testing connection to " + hostField.getText().trim() + "...");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    int port = Integer.parseInt(portField.getText().trim());
                    return DBConnection.testConnection(
                            hostField.getText().trim(),
                            port,
                            dbNameField.getText().trim(),
                            userField.getText().trim(),
                            new String(passField.getPassword())
                    );
                } catch (Exception ex) {
                    return ex.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String err = get();
                    if (err == null) {
                        statusLabel.setForeground(UIUtils.COLOR_SUCCESS);
                        statusLabel.setText("Connection successful!");
                        JOptionPane.showMessageDialog(DatabaseConfigDialog.this, 
                                "Connected successfully to MySQL database server!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setForeground(UIUtils.COLOR_DANGER);
                        statusLabel.setText("Connection failed!");
                        JOptionPane.showMessageDialog(DatabaseConfigDialog.this, 
                                "Could not connect to database:\n" + err + 
                                "\n\nPlease ensure MySQL is running on " + hostField.getText().trim() + " and allows LAN connections.", 
                                "Connection Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Test error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void initSchema() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to initialize the database and tables using database/schema.sql?\n" +
                "WARNING: This will create or reset tables in " + dbNameField.getText().trim() + "!",
                "Confirm Schema Initialization",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        saveSettingsQuietly();
        try {
            File schemaFile = new File("database/schema.sql");
            if (!schemaFile.exists()) {
                JOptionPane.showMessageDialog(this, "database/schema.sql not found at " + schemaFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            DBConnection.executeSqlScript(schemaFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Database schema and seed data initialized successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Schema initialization failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSettingsQuietly() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            DBConnection.saveConfig(
                    hostField.getText().trim(),
                    port,
                    dbNameField.getText().trim(),
                    userField.getText().trim(),
                    new String(passField.getPassword())
            );
        } catch (Exception ignored) {}
    }

    private void saveSettings() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            DBConnection.saveConfig(
                    hostField.getText().trim(),
                    port,
                    dbNameField.getText().trim(),
                    userField.getText().trim(),
                    new String(passField.getPassword())
            );
            statusLabel.setForeground(UIUtils.COLOR_SUCCESS);
            statusLabel.setText("Settings saved successfully.");
            JOptionPane.showMessageDialog(this, "Database configuration saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a valid integer number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
