

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern card component for displaying dashboard metrics and statistics.
 */
public class StatCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtextLabel;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtext, Color accentColor) {
        this.accentColor = accentColor;
        setOpaque(false);
        setLayout(new BorderLayout(8, 6));
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setPreferredSize(new Dimension(200, 115));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIUtils.FONT_SMALL);
        titleLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(UIUtils.FONT_CARD_NUM);
        valueLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(UIUtils.FONT_SMALL);
        subtextLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        centerPanel.add(subtextLabel);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtext(String subtext) {
        subtextLabel.setText(subtext);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Card Background
        g2.setColor(UIUtils.COLOR_CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

        // Subtle accent bar on the left
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(0, 8, 4, getHeight() - 16, 4, 4));

        // Border
        g2.setColor(UIUtils.COLOR_BORDER);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));

        g2.dispose();
        super.paintComponent(g);
    }
}
