package frontend.components;

import frontend.Theme;

import javax.swing.*;
import java.awt.*;

// Rounded card used to group content.
public class SurfacePanel extends JPanel {
    public SurfacePanel() {
        setOpaque(false);
        setBackground(Theme.SURFACE_ELEVATED);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Shadow
        g2.setColor(new Color(0,0,0,120));
        g2.fillRoundRect(4, 6, w - 8, h - 6, Theme.RADIUS, Theme.RADIUS);

        // Fill
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w - 8, h - 8, Theme.RADIUS, Theme.RADIUS);
        g2.dispose();
    }
}
