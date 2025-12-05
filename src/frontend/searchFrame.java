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
        styleCombo(filterBox);

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

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setFont(Theme.fontRegular(14));
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1));

        // White dropdown with black text, minimal highlight
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(Theme.fontRegular(14));
                if (isSelected) {
                    setBackground(new Color(240,240,240));
                    setForeground(Color.BLACK);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    private void doSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        String filter = (String) filterBox.getSelectedItem();
        if ("Title".equals(filter)) { filter = "All"; }
        resultsPanel.removeAll();
        try {
            // Special case: if filter is Genre and keyword is blank, show ALL media grouped by genre
            if ("Genre".equalsIgnoreCase((String) filterBox.getSelectedItem()) && (keyword == null || keyword.isEmpty())) {
                // Fetch all media and group by genre
                java.util.List all = BackendService.searchMedia("", "All");
                if (all == null || all.isEmpty()) {
                    JLabel empty = new JLabel("No results.");
                    empty.setForeground(Theme.TEXT_SECONDARY);
                    empty.setFont(Theme.fontRegular(14));
                    empty.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
                    resultsPanel.setLayout(new BorderLayout());
                    resultsPanel.add(empty, BorderLayout.NORTH);
                } else {
                    // Group by genre (alphabetical)
                    java.util.Map<String, java.util.List<Media>> byGenre = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    for (Object obj : all) {
                        Media m = (Media) obj;
                        String g = m.getGenre();
                        if (g == null || g.isBlank()) g = "Unknown";
                        byGenre.computeIfAbsent(g, k -> new java.util.ArrayList<>()).add(m);
                    }

                    resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
                    for (java.util.Map.Entry<String, java.util.List<Media>> entry : byGenre.entrySet()) {
                        String genre = entry.getKey();
                        java.util.List<Media> items = entry.getValue();

                        JPanel section = new JPanel(new BorderLayout());
                        section.setOpaque(false);

                        JLabel header = new JLabel(genre);
                        header.setForeground(Theme.TEXT_PRIMARY);
                        header.setFont(Theme.fontBold(18));
                        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
                        section.add(header, BorderLayout.NORTH);

                        // If more than 7 items in this genre, use horizontal row with arrows and cutoff like Member Home
                        final int visibleCount = 7;
                        if (items.size() > visibleCount) {
                            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                            row.setOpaque(false);
                            for (Media m : items) {
                                String t = m.getTitle();
                                Image img = ImageUtils.loadPosterImageForTitle(t, 160, 240);
                                ThumbnailCard card = new ThumbnailCard(t, img);
                                card.setOnClick(() -> nav.showMediaDetails(t));
                                row.add(card);
                            }
                            JScrollPane rowScroll = buildRowScroll(row, visibleCount);
                            section.add(buildScrollWithArrows(rowScroll, row, visibleCount), BorderLayout.CENTER);
                        } else {
                            // Otherwise, just lay them out wrapped under the label
                            JPanel cards = new JPanel(new WrapFlowLayout(FlowLayout.LEFT, 12, 12));
                            cards.setOpaque(false);
                            for (Media m : items) {
                                String t = m.getTitle();
                                Image img = ImageUtils.loadPosterImageForTitle(t, 160, 240);
                                ThumbnailCard card = new ThumbnailCard(t, img);
                                card.setOnClick(() -> nav.showMediaDetails(t));
                                cards.add(card);
                            }
                            section.add(cards, BorderLayout.CENTER);
                        }

                        resultsPanel.add(section);
                        resultsPanel.add(Box.createVerticalStrut(8));
                    }
                }
                resultsPanel.revalidate();
                resultsPanel.repaint();
                return;
            }

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
                    // Add only the ThumbnailCard — it already renders the title inside.
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

    // --- Helpers to build horizontal rows with arrows (same behavior as MemberHomePage) ---
    private JScrollPane buildRowScroll(JPanel row, int visibleCount) {
        JScrollPane rowScroll = new JScrollPane(row, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rowScroll.setBorder(null);
        rowScroll.setOpaque(false);
        rowScroll.getViewport().setOpaque(false);
        rowScroll.getHorizontalScrollBar().setUnitIncrement(16);
        int step = computeCardStepFromRow(row);
        int gap = 12;
        if (row.getLayout() instanceof FlowLayout) gap = ((FlowLayout) row.getLayout()).getHgap();
        int visibleW = Math.max(50, step * visibleCount - gap);
        int prefH = Math.max(row.getPreferredSize().height + 8, 260);
        Dimension viewDim = new Dimension(visibleW, prefH);
        rowScroll.setPreferredSize(viewDim);
        rowScroll.setMinimumSize(viewDim);
        rowScroll.setMaximumSize(viewDim);
        rowScroll.addMouseWheelListener(e -> {
            if (e.isShiftDown()) {
                JScrollBar hb = rowScroll.getHorizontalScrollBar();
                int delta = e.getWheelRotation() * hb.getUnitIncrement();
                hb.setValue(Math.max(0, Math.min(hb.getMaximum(), hb.getValue() + delta * 8)));
                e.consume();
            }
        });
        return rowScroll;
    }

    private JPanel buildScrollWithArrows(JScrollPane rowScroll, JPanel row, int visibleCount) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JButton left = makeArrowButton("◄");
        JButton right = makeArrowButton("►");

        int step = computeCardStepFromRow(row);
        int gap = 12;
        if (row.getLayout() instanceof FlowLayout) gap = ((FlowLayout) row.getLayout()).getHgap();
        int visibleW = Math.max(50, step * visibleCount - gap);

        int gutter = 8;
        int leftW = left.getPreferredSize().width + gutter;
        int rightW = right.getPreferredSize().width + gutter;
        int overlayReserve = 16;

        Dimension contSize = new Dimension(visibleW + leftW + rightW + overlayReserve, rowScroll.getPreferredSize().height);
        container.setPreferredSize(contSize);
        container.setMinimumSize(contSize);
        container.setMaximumSize(contSize);

        container.add(rowScroll, BorderLayout.CENTER);

        Runnable updateButtons = () -> SwingUtilities.invokeLater(() -> {
            JScrollBar hb = rowScroll.getHorizontalScrollBar();
            int val = hb.getValue();
            int max = hb.getMaximum() - hb.getVisibleAmount();
            left.setEnabled(val > 0);
            right.setEnabled(val < max);
        });

        left.addActionListener(e -> {
            JScrollBar hb = rowScroll.getHorizontalScrollBar();
            int stepPx = computeCardStep(rowScroll);
            hb.setValue(Math.max(0, hb.getValue() - stepPx));
            updateButtons.run();
        });
        right.addActionListener(e -> {
            JScrollBar hb = rowScroll.getHorizontalScrollBar();
            int stepPx = computeCardStep(rowScroll);
            int max = hb.getMaximum() - hb.getVisibleAmount();
            hb.setValue(Math.min(max, hb.getValue() + stepPx));
            updateButtons.run();
        });

        rowScroll.getHorizontalScrollBar().addAdjustmentListener(e -> updateButtons.run());
        updateButtons.run();

        JPanel west = new JPanel(new GridBagLayout());
        west.setOpaque(false);
        west.setPreferredSize(new Dimension(leftW, contSize.height));
        west.setMinimumSize(new Dimension(leftW, 0));
        west.setMaximumSize(new Dimension(leftW, Integer.MAX_VALUE));
        west.setBorder(BorderFactory.createEmptyBorder(0, gutter / 2, 0, gutter / 2));
        west.add(left);
        JPanel east = new JPanel(new GridBagLayout());
        east.setOpaque(false);
        east.setPreferredSize(new Dimension(rightW, contSize.height));
        east.setMinimumSize(new Dimension(rightW, 0));
        east.setMaximumSize(new Dimension(rightW, Integer.MAX_VALUE));
        east.setBorder(BorderFactory.createEmptyBorder(0, gutter / 2, 0, gutter / 2));
        east.add(right);

        container.add(west, BorderLayout.WEST);
        container.add(east, BorderLayout.EAST);

        JPanel outer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        outer.setOpaque(false);
        outer.add(container);
        return outer;
    }

    private JButton makeArrowButton(String text) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setMargin(new Insets(8, 14, 8, 14));
        b.setBackground(Theme.ACCENT_GOLD);
        b.setForeground(Color.BLACK);
        b.setFont(Theme.fontBold(18));
        b.setBorder(BorderFactory.createLineBorder(new Color(180, 150, 0), 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setToolTipText(text.contains("◄") ? "Scroll left" : "Scroll right");
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(Theme.ACCENT_GOLD.darker()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(Theme.ACCENT_GOLD); }
        });
        return b;
    }

    private int computeCardStep(JScrollPane rowScroll) {
        Component view = rowScroll.getViewport().getView();
        if (view instanceof JPanel) {
            JPanel row = (JPanel) view;
            int gap = 12;
            if (row.getLayout() instanceof FlowLayout) {
                gap = ((FlowLayout) row.getLayout()).getHgap();
            }
            if (row.getComponentCount() > 0) {
                Component c = row.getComponent(0);
                int w = c.getPreferredSize().width;
                return Math.max(10, w + gap);
            }
            return 160 + gap;
        }
        return 172;
    }

    private int computeCardStepFromRow(JPanel row) {
        int gap = 12;
        if (row.getLayout() instanceof FlowLayout) {
            gap = ((FlowLayout) row.getLayout()).getHgap();
        }
        if (row.getComponentCount() > 0) {
            Component c = row.getComponent(0);
            int w = c.getPreferredSize().width;
            return Math.max(10, w + gap);
        }
        return 172;
    }
}
