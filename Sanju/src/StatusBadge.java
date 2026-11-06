

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern colored pill badge for displaying entity statuses.
 */
public class StatusBadge extends JLabel {

    private Color bgColor;
    private Color textColor;

    public StatusBadge(String status) {
        super(status.toUpperCase(), SwingConstants.CENTER);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setBorder(new EmptyBorder(4, 10, 4, 10));
        setStatus(status);
    }

    public void setStatus(String status) {
        setText(status.toUpperCase());
        String s = status.toUpperCase();
        if (s.contains("ACTIVE") || s.contains("LEADING") || s.contains("WINNER")) {
            bgColor = new Color(16, 185, 129, 45);
            textColor = UIUtils.COLOR_SUCCESS;
        } else if (s.contains("CLOSED") || s.contains("INACTIVE")) {
            bgColor = new Color(239, 68, 68, 45);
            textColor = UIUtils.COLOR_DANGER;
        } else if (s.contains("UPCOMING") || s.contains("TIE")) {
            bgColor = new Color(245, 158, 11, 45);
            textColor = UIUtils.COLOR_WARNING;
        } else {
            bgColor = new Color(71, 85, 105, 45);
            textColor = UIUtils.COLOR_TEXT_SECONDARY;
        }
        setForeground(textColor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor != null ? bgColor : UIUtils.COLOR_CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.setColor(textColor != null ? textColor : UIUtils.COLOR_TEXT_PRIMARY);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
        g2.dispose();
        super.paintComponent(g);
    }
}
