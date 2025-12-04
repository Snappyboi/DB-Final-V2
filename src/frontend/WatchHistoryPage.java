package frontend;

import backend.BackendService;
import backend.Media;
import frontend.components.ImageUtils;
import frontend.components.NavBar;
import frontend.components.ThumbnailCard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Watch History: shows what the user watched.
public class WatchHistoryPage extends JPanel {
    private final Navigation nav;
    private JPanel resultsPanel;

    public WatchHistoryPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 12, 12));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JLabel title = new JLabel("My Watch History");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));
        content.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.setBackground(Theme.BACKGROUND);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    // Load history and show cards
    public void refresh() {
        resultsPanel.removeAll();
        String username = nav.getCurrentUsername();
        List<Media> history;
        try {
            history = BackendService.getWatchHistory(username);
        } catch (Exception e) {
            history = new ArrayList<>();
        }
        if (history == null || history.isEmpty()) {
            JLabel empty = new JLabel("No watch history yet.");
            empty.setForeground(Theme.TEXT_SECONDARY);
            empty.setFont(Theme.fontRegular(14));
            empty.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
            resultsPanel.setLayout(new BorderLayout());
            resultsPanel.add(empty, BorderLayout.NORTH);
        } else {
            resultsPanel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 12, 12));
            for (Media m : history) {
                String t = m.getTitle();
                Image img = ImageUtils.loadPosterImageForTitle(t, 160, 240);
                ThumbnailCard card = new ThumbnailCard(t, img);
                card.setOnClick(() -> nav.showMediaDetails(t));
                resultsPanel.add(card);
            }
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // FlowLayout that wraps to the next line
    static class WrapFlowLayout extends FlowLayout {
        public WrapFlowLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override
        public Dimension minimumLayoutSize(Container target) { Dimension d = layoutSize(target, false); d.width -= (getHgap() + 1); return d; }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxWidth = target.getWidth();
                if (maxWidth == 0) maxWidth = Integer.MAX_VALUE;
                Insets insets = target.getInsets();
                int hgap = getHgap();
                int vgap = getVgap();
                int x = 0; int y = insets.top + vgap; int rowHeight = 0; int reqWidth = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x == 0 || x + d.width <= maxWidth - insets.right) {
                        if (x > 0) x += hgap;
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        y += rowHeight + vgap;
                        reqWidth = Math.max(reqWidth, x);
                        x = d.width;
                        rowHeight = d.height;
                    }
                }
                y += rowHeight + insets.bottom + vgap;
                reqWidth = Math.max(reqWidth, x) + insets.left + insets.right;
                return new Dimension(reqWidth, y);
            }
        }
    }
}
