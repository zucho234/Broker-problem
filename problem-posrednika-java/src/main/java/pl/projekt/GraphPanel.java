package pl.projekt;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class GraphPanel extends JPanel {
    private TransportProblem problem;
    private IterationStep step;

    public GraphPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(520, 520));
    }

    public void showStep(TransportProblem problem, IterationStep step) {
        this.problem = problem;
        this.step = step;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (problem == null) {
            drawCenteredText(g, "Graf pojawi się po obliczeniu rozwiązania.");
            g.dispose();
            return;
        }

        int suppliers = problem.getSupplierCount();
        int receivers = problem.getReceiverCount();
        Point[] supplierPoints = createSidePoints(suppliers, 90);
        Point[] receiverPoints = createSidePoints(receivers, getWidth() - 90);

        drawEdges(g, supplierPoints, receiverPoints);
        drawNodes(g, supplierPoints, true);
        drawNodes(g, receiverPoints, false);

        g.dispose();
    }

    private void drawEdges(Graphics2D g, Point[] supplierPoints, Point[] receiverPoints) {
        boolean[][] blocked = problem.getBlocked();
        int[][] profits = problem.calculateProfitMatrix();
        int[][] allocation = step == null ? new int[problem.getSupplierCount()][problem.getReceiverCount()] : step.getAllocationSnapshot();

        Stroke normalStroke = new BasicStroke(1.3f);
        Stroke blockedStroke = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{7f, 7f}, 0f);
        Stroke allocationStroke = new BasicStroke(3.2f);
        Stroke selectedStroke = new BasicStroke(4.2f);

        for (int i = 0; i < supplierPoints.length; i++) {
            for (int j = 0; j < receiverPoints.length; j++) {
                boolean selected = step != null && step.getSupplier() == i && step.getReceiver() == j;
                int amount = allocation[i][j];

                if (blocked[i][j]) {
                    g.setColor(new Color(190, 70, 70));
                    g.setStroke(blockedStroke);
                } else if (selected) {
                    g.setColor(new Color(230, 126, 34));
                    g.setStroke(selectedStroke);
                } else if (amount > 0) {
                    g.setColor(new Color(39, 116, 174));
                    g.setStroke(allocationStroke);
                } else {
                    g.setColor(new Color(215, 220, 225));
                    g.setStroke(normalStroke);
                }

                Point from = supplierPoints[i];
                Point to = receiverPoints[j];
                g.drawLine(from.x, from.y, to.x, to.y);

                if (amount > 0 || selected || blocked[i][j]) {
                    drawEdgeLabel(g, from, to, amount, profits[i][j], blocked[i][j]);
                }
            }
        }
    }

    private void drawEdgeLabel(Graphics2D g, Point from, Point to, int amount, int profit, boolean blocked) {
        String text = blocked ? "X" : amount + " | z=" + formatProfit(profit);
        int x = (from.x + to.x) / 2;
        int y = (from.y + to.y) / 2;

        Font oldFont = g.getFont();
        g.setFont(oldFont.deriveFont(Font.BOLD, 11f));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text) + 10;
        int height = metrics.getHeight() + 4;

        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(x - width / 2, y - height / 2, width, height, 8, 8);
        g.setColor(blocked ? new Color(160, 50, 50) : new Color(40, 65, 82));
        g.drawString(text, x - metrics.stringWidth(text) / 2, y + metrics.getAscent() / 2 - 2);
        g.setFont(oldFont);
    }

    private void drawNodes(Graphics2D g, Point[] points, boolean suppliers) {
        int radius = 34;
        int[] remaining = null;
        int[] original = suppliers ? problem.getSupply() : problem.getDemand();

        if (step != null) {
            remaining = suppliers ? step.getRemainingSupply() : step.getRemainingDemand();
        }

        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            boolean fake = suppliers ? problem.isFakeSupplier(i) : problem.isFakeReceiver(i);
            String name = nodeName(suppliers, i, fake);

            g.setColor(fake ? new Color(247, 244, 232) : new Color(232, 243, 252));
            g.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
            g.setColor(fake ? new Color(151, 124, 54) : new Color(35, 92, 133));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(point.x - radius, point.y - radius, radius * 2, radius * 2);

            drawCentered(g, name, point.x, point.y - 5, Font.BOLD, 15f, new Color(30, 45, 55));
            String value = remaining == null ? String.valueOf(original[i]) : remaining[i] + "/" + original[i];
            drawCentered(g, value, point.x, point.y + 14, Font.PLAIN, 11f, new Color(75, 85, 95));
        }
    }

    private Point[] createSidePoints(int count, int x) {
        Point[] points = new Point[count];
        int top = 70;
        int bottom = Math.max(top + 1, getHeight() - 70);

        for (int i = 0; i < count; i++) {
            int y = count == 1 ? getHeight() / 2 : top + (bottom - top) * i / (count - 1);
            points[i] = new Point(x, y);
        }

        return points;
    }

    private void drawCenteredText(Graphics2D g, String text) {
        drawCentered(g, text, getWidth() / 2, getHeight() / 2, Font.PLAIN, 15f, new Color(90, 98, 107));
    }

    private void drawCentered(Graphics2D g, String text, int x, int y, int style, float size, Color color) {
        Font oldFont = g.getFont();
        g.setFont(oldFont.deriveFont(style, size));
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(color);
        g.drawString(text, x - metrics.stringWidth(text) / 2, y);
        g.setFont(oldFont);
    }

    private String nodeName(boolean supplier, int index, boolean fake) {
        if (fake) {
            return supplier ? "Fd" : "Fo";
        }
        return (supplier ? "D" : "O") + (index + 1);
    }

    private String formatProfit(int profit) {
        if (profit <= TransportProblem.BIG_NEGATIVE / 2) {
            return "-M";
        }
        return String.valueOf(profit);
    }
}
