package frontend.components;

import frontend.Theme;
import frontend.components.ImageUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.image.BufferedImage;

/**
 * Small helpers to render posters/avatars inside admin tables.
 */
public final class AdminTableRenderers {
    private AdminTableRenderers() {}

    private static final Map<String, ImageIcon> posterCache = new ConcurrentHashMap<>();
    private static final Map<String, ImageIcon> avatarCache = new ConcurrentHashMap<>();

    public static DefaultTableCellRenderer posterWithTitleRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String title = value == null ? "" : value.toString();
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, title, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setIcon(loadPosterIcon(title));
                lbl.setIconTextGap(8);
                lbl.setForeground(Theme.TEXT_PRIMARY);
                lbl.setOpaque(isSelected);
                return lbl;
            }
        };
    }

    public static DefaultTableCellRenderer avatarWithNameRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String name = value == null ? "" : value.toString();
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, name, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setIcon(loadAvatarIcon(name));
                lbl.setIconTextGap(8);
                lbl.setForeground(Theme.TEXT_PRIMARY);
                lbl.setOpaque(isSelected);
                return lbl;
            }
        };
    }

    private static ImageIcon loadPosterIcon(String title) {
        if (title == null || title.isBlank()) return null;
        return posterCache.computeIfAbsent(title, t -> {
            Image img = ImageUtils.loadPosterImageForTitle(t, 54, 80);
            return (img == null) ? null : new ImageIcon(img);
        });
    }

    private static ImageIcon loadAvatarIcon(String name) {
        final String safeName = (name == null) ? "" : name;
        final String key = safeName;
        return avatarCache.computeIfAbsent(key, n -> {
            int size = 28;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(60, 90, 160));
            g2.fillOval(0, 0, size, size);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.fontBold(14));
            String letter = safeName.isBlank() ? "?" : safeName.substring(0, 1).toUpperCase();
            FontMetrics fm = g2.getFontMetrics();
            int tx = (size - fm.stringWidth(letter)) / 2;
            int ty = (size + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(letter, tx, ty);
            g2.dispose();
            return new ImageIcon(img);
        });
    }
}
