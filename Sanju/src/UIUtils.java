
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UI styling utilities for modern, polished Java Swing desktop application.
 */
public class UIUtils {

    // Modern Dark-Slate & Indigo Color Palette
    public static final Color COLOR_BG_DARK       = new Color(15, 23, 42);    // Slate 900
    public static final Color COLOR_SIDEBAR       = new Color(24, 33, 56);    // Dark Navy
    public static final Color COLOR_CARD_BG       = new Color(30, 41, 59);    // Slate 800
    public static final Color COLOR_CARD_HOVER    = new Color(40, 53, 75);    // Slate 750
    public static final Color COLOR_SURFACE_LIGHT = new Color(51, 65, 85);    // Slate 700
    public static final Color COLOR_BORDER        = new Color(71, 85, 105);   // Slate 600

    public static final Color COLOR_PRIMARY       = new Color(59, 130, 246);  // Blue 500
    public static final Color COLOR_PRIMARY_HOVER = new Color(37, 99, 235);   // Blue 600
    public static final Color COLOR_SUCCESS       = new Color(16, 185, 129);  // Emerald 500
    public static final Color COLOR_SUCCESS_HOVER = new Color(5, 150, 105);   // Emerald 600
    public static final Color COLOR_WARNING       = new Color(245, 158, 11);  // Amber 500
    public static final Color COLOR_DANGER        = new Color(239, 68, 68);   // Red 500
    public static final Color COLOR_DANGER_HOVER  = new Color(220, 38, 38);   // Red 600
    public static final Color COLOR_GOLD          = new Color(251, 191, 36);  // Gold 400

    public static final Color COLOR_TEXT_PRIMARY   = new Color(248, 250, 252); // Slate 50
    public static final Color COLOR_TEXT_SECONDARY = new Color(148, 163, 184); // Slate 400
    public static final Color COLOR_TEXT_MUTED     = new Color(100, 116, 139); // Slate 500

    // Typography
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SECTION  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_CARD_NUM = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Creates a styled modern JButton with rounded corners and hover effect.
     */
    public static JButton createButton(String text, Color bgColor, Color hoverColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color currentBg = getModel().isPressed() ? hoverColor.darker() : 
                                  getModel().isRollover() ? hoverColor : bgColor;
                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BOLD);
        button.setForeground(fgColor);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, COLOR_DANGER, COLOR_DANGER_HOVER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, COLOR_SURFACE_LIGHT, COLOR_BORDER, COLOR_TEXT_PRIMARY);
    }

    /**
     * Styles JTextField for sleek modern input.
     */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setBackground(new Color(30, 41, 59));
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
    }

    /**
     * Styles JTable with modern headers, padding, row heights, and selection colors.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setBackground(COLOR_CARD_BG);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setGridColor(new Color(51, 65, 85));
        table.setSelectionBackground(new Color(59, 130, 246, 120));
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SECTION);
        header.setBackground(COLOR_SIDEBAR);
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(COLOR_CARD_BG);
        centerRenderer.setForeground(COLOR_TEXT_PRIMARY);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                    setBorder(new EmptyBorder(0, 10, 0, 10));
                    if (!isSel) {
                        setBackground(r % 2 == 0 ? COLOR_CARD_BG : new Color(26, 36, 52));
                        setForeground(COLOR_TEXT_PRIMARY);
                    }
                    return comp;
                }
            });
        }
    }

    /**
     * Creates a rounded card panel.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }
}
