// Search/Browse page
package frontend;

import backend.BackendService;
import backend.Media;
import frontend.components.ImageUtils;
import frontend.components.NavBar;
import frontend.components.ThumbnailCard;
import frontend.components.RoundedButton;

import javax.swing.*;
import java.awt.*;

public class searchFrame extends JPanel {
    private final Navigation nav;
    private JTextField searchField;
    private JComboBox<String> filterBox;
    private JPanel resultsPanel;

    public searchFrame(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        // Top bar with nav + search row
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(new NavBar(nav, true), BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        searchRow.setOpaque(false);

        searchField = new JTextField(28);
        styleField(searchField);

        filterBox = new JComboBox<>(new String[]{
                "All", "Title", "Movies", "Series", "Actor", "Genre", "Director"
        });
        filterBox.setBackground(Theme.SURFACE);
        filterBox.setForeground(Theme.TEXT_PRIMARY);
        filterBox.setFont(Theme.fontRegular(14));
        // Items show white when open; selected is black when closed
        filterBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index >= 0) {
                    c.setBackground(isSelected ? list.getSelectionBackground() : Theme.SURFACE);
                    c.setForeground(Theme.TEXT_PRIMARY);
                } else {
                    c.setBackground(Theme.SURFACE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        RoundedButton searchBtn = new RoundedButton("Search").gold();
        searchBtn.addActionListener(e -> doSearch());

        searchRow.add(searchField);
        searchRow.add(filterBox);
        searchRow.add(searchBtn);
        top.add(searchRow, BorderLayout.CENTER);
        add(top, BorderLayout.PAGE_START);

        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 12, 12));

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void styleField(JTextField f) {
        f.setBackground(Theme.SURFACE);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        f.setFont(Theme.fontRegular(14));
    }

    public void focusSearch() {
        SwingUtilities.invokeLater(() -> {
            searchField.requestFocusInWindow();
            searchField.selectAll();
        });
    }

    private void doSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        String filter = (String) filterBox.getSelectedItem();
        if ("Title".equals(filter)) { filter = "All"; }
        resultsPanel.removeAll();
        try {
            java.util.List results = BackendService.searchMedia(keyword, filter);
            if (results == null || results.isEmpty()) {
                JLabel empty = new JLabel("No results.");
                empty.setForeground(Theme.TEXT_SECONDARY);
                empty.setFont(Theme.fontRegular(14));
                empty.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
                resultsPanel.setLayout(new BorderLayout());
                resultsPanel.add(empty, BorderLayout.NORTH);
            } else {
                resultsPanel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 12, 12));
                for (Object obj : results) {
                    Media m = (Media) obj;
                    String t = m.getTitle();
                    Image img = ImageUtils.loadPosterImageForTitle(t, 160, 240);
                    ThumbnailCard card = new ThumbnailCard(t, img);
                    card.setOnClick(() -> nav.showMediaDetails(t));
                    resultsPanel.add(card);
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("Search error.");
            err.setForeground(Color.RED);
            err.setFont(Theme.fontRegular(14));
            err.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
            resultsPanel.setLayout(new BorderLayout());
            resultsPanel.add(err, BorderLayout.NORTH);
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // Flow layout that wraps to next line
    static class WrapFlowLayout extends FlowLayout {
        public WrapFlowLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { Dimension d = layoutSize(target, false); d.width -= (getHgap() + 1); return d; }
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
