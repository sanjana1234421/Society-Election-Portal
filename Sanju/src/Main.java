
import javax.swing.*;
import java.awt.*;

/**
 * Main application launcher for Society Voting & Election Management System.
 * Initializes desktop styling, validates LAN database connectivity, and launches
 * the authentication portal.
 */
public class Main {

    public static void main(String[] args) {
        // Enhance text rendering and antialiasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        // Apply dark palette defaults to JOptionPane and dialogs
        UIManager.put("OptionPane.background", UIUtils.COLOR_CARD_BG);
        UIManager.put("Panel.background", UIUtils.COLOR_BG_DARK);
        UIManager.put("OptionPane.messageForeground", UIUtils.COLOR_TEXT_PRIMARY);

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);

            // Asynchronously check database connection on startup
            SwingWorker<String, Void> checkDbWorker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return DBConnection.testCurrentConnection();
                }

                @Override
                protected void done() {
                    try {
                        String error = get();
                        if (error != null) {
                            int choice = JOptionPane.showConfirmDialog(loginFrame,
                                    "Could not connect to MySQL server at '" + DBConnection.getDbHost() + "':\n\n" +
                                    error + "\n\n" +
                                    "Would you like to open LAN Database Settings now to configure the host IP or initialize the database?",
                                    "Database Connection Warning",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (choice == JOptionPane.YES_OPTION) {
                                new DatabaseConfigDialog(loginFrame).setVisible(true);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            };
            checkDbWorker.execute();
        });
    }
}
