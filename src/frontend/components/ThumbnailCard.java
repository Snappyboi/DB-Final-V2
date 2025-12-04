package frontend.components;

import frontend.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

//  poster card with a title. Clickable.
public class ThumbnailCard extends JPanel {
    private final String title;
    private Image image;
    private Runnable onClick;

    public ThumbnailCard(String title, Image image) {
        this.title = title;
        this.image = image;
        setOpaque(false);
        setPreferredSize(new Dimension(160, 240));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e) { repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
    }

    public void setOnClick(Runnable onClick) { this.onClick = onClick; }

    public void setImage(Image image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Card rectangle
        int cardW = w - 8;
        int cardH = h - 36;

        // Hover glow
        if (getMousePosition() != null) {
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRoundRect(4, 4, cardW, cardH, Theme.RADIUS, Theme.RADIUS);
        }

        // Poster placeholder or image
        Shape clip = new RoundRectangle2D.Float(4, 4, cardW, cardH, Theme.RADIUS, Theme.RADIUS);
        g2.setClip(clip);
        if (image != null) {
            g2.drawImage(image, 4, 4, cardW, cardH, null);
        } else {
            g2.setColor(Theme.SURFACE);
            g2.fillRect(4, 4, cardW, cardH);
            g2.setColor(new Color(255,255,255,30));
            g2.setFont(Theme.fontMedium(16));
            String t = "ACED";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, 4 + (cardW - fm.stringWidth(t))/2, 4 + (cardH + fm.getAscent())/2 - 8);
        }
        g2.setClip(null);

        // Title text
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.setFont(Theme.fontRegular(13));
        FontMetrics fm2 = g2.getFontMetrics();
        int y = cardH + 22;
        String show = title;
        if (fm2.stringWidth(show) > w - 8) {
            while (show.length() > 0 && fm2.stringWidth(show + "…") > w - 8) {
                show = show.substring(0, show.length() - 1);
            }
            show += "…";
        }
        g2.drawString(show, 4, y);
        g2.dispose();
    }
}
