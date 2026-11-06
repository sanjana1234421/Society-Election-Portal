

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Graphical Horizontal Bar Chart component for Real-Time Vote Dashboard.
 * Renders animated-feeling antialiased bars with gradient styling, candidate symbols,
 * percentages, vote tallies, and dynamic highlighting for leading candidates.
 */
public class VoteBarChartPanel extends JPanel {

    private List<LiveVoteStat> stats = new ArrayList<>();
    private int totalVotes = 0;

    public VoteBarChartPanel() {
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(500, 260));
    }

    public synchronized void updateData(List<LiveVoteStat> newStats, int totalVotes) {
        this.stats = new ArrayList<>(newStats);
        this.totalVotes = totalVotes;
        // Dynamically calculate preferred height based on candidate count
        int calculatedHeight = Math.max(220, stats.size() * 55 + 50);
        setPreferredSize(new Dimension(getWidth() > 0 ? getWidth() : 500, calculatedHeight));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background container
        g2.setColor(UIUtils.COLOR_CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 16, 16));
        g2.setColor(UIUtils.COLOR_BORDER);
        g2.draw(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 16, 16));

        // Header Title
        g2.setFont(UIUtils.FONT_SECTION);
        g2.setColor(UIUtils.COLOR_TEXT_PRIMARY);
        g2.drawString("LIVE CANDIDATE STANDINGS & VOTE SHARE", 20, 30);

        List<LiveVoteStat> currentStats;
        synchronized (this) {
            currentStats = new ArrayList<>(stats);
        }

        if (currentStats == null || currentStats.isEmpty()) {
            g2.setFont(UIUtils.FONT_REGULAR);
            g2.setColor(UIUtils.COLOR_TEXT_MUTED);
            g2.drawString("No candidates or voting data available yet.", 20, 65);
            g2.dispose();
            return;
        }

        int startY = 60;
        int barHeight = 24;
        int rowGap = 52;
        int labelWidth = 180;
        int maxBarWidth = Math.max(80, width - labelWidth - 140);

        // Find max votes to scale bars proportionally
        int maxVotes = 0;
        for (LiveVoteStat s : currentStats) {
            if (s.getVoteCount() > maxVotes) {
                maxVotes = s.getVoteCount();
            }
        }

        for (int i = 0; i < currentStats.size(); i++) {
            LiveVoteStat s = currentStats.get(i);
            int y = startY + (i * rowGap);

            if (y + barHeight > height - 10) {
                break; // Prevent clipping
            }

            // Draw Candidate Name & Symbol
            g2.setFont(UIUtils.FONT_BOLD);
            g2.setColor(s.isLeading() ? UIUtils.COLOR_GOLD : UIUtils.COLOR_TEXT_PRIMARY);

            String candidateLabel = s.getCandidateName();
            if (candidateLabel.length() > 18) {
                candidateLabel = candidateLabel.substring(0, 16) + "..";
            }
            g2.drawString(candidateLabel, 20, y + 17);

            // Draw Symbol Tag
            g2.setFont(UIUtils.FONT_SMALL);
            g2.setColor(UIUtils.COLOR_TEXT_SECONDARY);
            g2.drawString("[" + s.getSymbol() + "]", labelWidth - 55, y + 17);

            // Bar background track
            int barStartX = labelWidth + 10;
            g2.setColor(UIUtils.COLOR_SURFACE_LIGHT);
            g2.fill(new RoundRectangle2D.Float(barStartX, y, maxBarWidth, barHeight, 10, 10));

            // Bar fill width
            double ratio = maxVotes > 0 ? (double) s.getVoteCount() / maxVotes : 0.0;
            int barFillWidth = (int) (ratio * maxBarWidth);
            if (s.getVoteCount() > 0 && barFillWidth < 8) {
                barFillWidth = 8;
            }

            // Fill gradient
            if (barFillWidth > 0) {
                GradientPaint gradient;
                if (s.isLeading()) {
                    gradient = new GradientPaint(
                            barStartX, y, new Color(245, 158, 11),
                            barStartX + barFillWidth, y, new Color(251, 191, 36)
                    );
                } else {
                    gradient = new GradientPaint(
                            barStartX, y, new Color(59, 130, 246),
                            barStartX + barFillWidth, y, new Color(14, 165, 233)
                    );
                }
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(barStartX, y, barFillWidth, barHeight, 10, 10));
            }

            // Value label: Votes & Percentage
            g2.setFont(UIUtils.FONT_BOLD);
            g2.setColor(UIUtils.COLOR_TEXT_PRIMARY);
            String valueText = s.getVoteCount() + " (" + String.format("%.1f%%", s.getVotePercentage()) + ")";
            g2.drawString(valueText, barStartX + maxBarWidth + 12, y + 17);

            // Leading Indicator Badge
            if (s.isLeading() && s.getVoteCount() > 0) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(UIUtils.COLOR_GOLD);
                g2.drawString(" LEADING", barStartX + maxBarWidth + 12, y + 33);
            }
        }

        g2.dispose();
    }
}
