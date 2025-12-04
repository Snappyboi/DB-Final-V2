package frontend.components;

import frontend.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// button with hover
public class RoundedButton extends JButton {
    private Color bgColor = Theme.BUTTON_BG;
    private Color hoverColor = Theme.BUTTON_BG_HOVER;
    private Color textColor = Theme.TEXT_PRIMARY;
    private boolean accentRed = false;
    private boolean accentGold = false;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(Theme.fontMedium(14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e) { repaint(); }
            @Override public void mousePressed(MouseEvent e) { repaint(); }
            @Override public void mouseReleased(MouseEvent e) { repaint(); }
        });
    }

    public RoundedButton red() { this.accentRed = true; this.accentGold = false; return this; }
    public RoundedButton gold() { this.accentGold = true; this.accentRed = false; return this; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = accentRed ? Theme.ACCENT_RED : accentGold ? Theme.ACCENT_GOLD : bgColor;
        Color hover = accentRed || accentGold ? base.darker() : hoverColor;

        boolean hoverState = getModel().isRollover();
        boolean pressed = getModel().isArmed();

        Color fill = base;
        if (hoverState) fill = hover;
        if (pressed) fill = fill.darker();

        int w = getWidth();
        int h = getHeight();

        // Shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(2, 3, w - 4, h - 3, Theme.RADIUS, Theme.RADIUS);

        // Button shape
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - 4, h - 6, Theme.RADIUS, Theme.RADIUS);

        // Text
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textW = fm.stringWidth(getText());
        int textH = fm.getAscent();
        g2.setColor(textColor);
        g2.drawString(getText(), (w - textW) / 2 - 2, (h + textH) / 2 - 8);
        g2.dispose();
    }
}
