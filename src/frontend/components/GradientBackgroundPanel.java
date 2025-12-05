package frontend.components;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable background panel with the navy gradient used on the login screen.
 */
public class GradientBackgroundPanel extends JPanel {
    public GradientBackgroundPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public GradientBackgroundPanel() {
        super();
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(8, 14, 32), 0, h, new Color(18, 28, 60));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}
