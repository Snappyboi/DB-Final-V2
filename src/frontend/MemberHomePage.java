package frontend;

import backend.BackendService;
import backend.Media;
import frontend.components.NavBar;
import frontend.components.ImageUtils;
import frontend.components.ThumbnailCard;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Home page with sections
public class MemberHomePage extends JPanel {
    private final Navigation nav;

    public MemberHomePage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));

        // Browse row
        content.add(postersRow("Browse", posterFiles(), 7));
        content.add(Box.createVerticalStrut(16));

        // Trending from DB
        content.add(trendingRowFromDB("Trending Now", 7));
        content.add(Box.createVerticalStrut(16));
        // New Releases rows (based on highest media_ID values) — match Browse/Trending cutoff & arrows
        content.add(newReleasesMoviesRow("New Movies", 7));
        content.add(Box.createVerticalStrut(16));
        content.add(newReleasesSeriesRow("New Series", 7));

        // Page scroll
        JScrollPane scroll = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);


        JScrollBar vbarRight = createOverlayVScroll(scroll);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(scroll, BorderLayout.CENTER);
        center.add(vbarRight, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);
    }

    private JPanel trendingRowFromDB(String title, int visibleCount) {
        List<Media> items;
        try {
            // Fetch more than we show so arrows can reveal additional items
            items = BackendService.getTrendingMedia(50);
        } catch (Exception e) {
            items = new ArrayList<>();
        }

        if (items == null || items.isEmpty()) {
            // Fallback to mock titles if DB returns nothing
            return section(title, mockTitles("Trending", 24), visibleCount);
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(Theme.fontBold(18));
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        wrap.add(label, BorderLayout.NORTH);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        // Add all items
        for (Media m : items) {
            String t = m.getTitle();
            ThumbnailCard card = new ThumbnailCard(t, null);
            // Try to load poster by title
            Image img = ImageUtils.loadPosterImageForTitle(t, 160, 240);
            if (img != null) card.setImage(img);
            card.setOnClick(() -> nav.showMediaDetails(t));
            row.add(card);
        }

        JScrollPane rowScroll = buildRowScroll(row, visibleCount);
        wrap.add(buildScrollWithArrows(rowScroll, row, visibleCount), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel postersRow(String title, List<String> files, int visibleCount) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(Theme.fontBold(18));
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        wrap.add(label, BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);

        for (String f : files) {
            String titleFromFile = stripExtension(f);
            Image img = ImageUtils.loadPosterImage("/MoviePosters/" + f, 160, 240);
            ThumbnailCard card = new ThumbnailCard(titleFromFile, img);
            card.setOnClick(() -> nav.showMediaDetails(titleFromFile));
            row.add(card);
        }

        JScrollPane rowScroll = buildRowScroll(row, visibleCount);
        wrap.add(buildScrollWithArrows(rowScroll, row, visibleCount), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel newReleasesMoviesRow(String title, int visibleCount) {
        List<Media> items;
        try {
            items = BackendService.getNewestMovies(15);
        } catch (Exception e) {
            items = new ArrayList<>();
        }
        return buildMediaRow(title, items, visibleCount);
    }

    private JPanel newReleasesSeriesRow(String title, int visibleCount) {
        List<Media> items;
        try {
            items = BackendService.getNewestSeries(15);
        } catch (Exception e) {
            items = new ArrayList<>();
        }
        return buildMediaRow(title, items, visibleCount);
    }

    private JPanel buildMediaRow(String title, List<Media> items, int visibleCount) {
        if (items == null || items.isEmpty()) {
            // Fallback to mock titles if no data
            return section(title, mockTitles(title, 14), visibleCount);
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(Theme.fontBold(18));
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        wrap.add(label, BorderLayout.NORTH);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        for (Media m : items) {
            String t = m.getTitle();
            Image img = ImageUtils.loadPosterImageForTitle(t, 160, 240);
            ThumbnailCard card = new ThumbnailCard(t, img);
            card.setOnClick(() -> nav.showMediaDetails(t));
            row.add(card);
        }

        JScrollPane rowScroll = buildRowScroll(row, visibleCount);
        wrap.add(buildScrollWithArrows(rowScroll, row, visibleCount), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel section(String title, List<String> items, int visibleCount) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BorderLayout());
        wrap.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(Theme.fontBold(18));
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        wrap.add(label, BorderLayout.NORTH);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        for (String t : items) {
            ThumbnailCard card = new ThumbnailCard(t, null);
            card.setOnClick(() -> nav.showMediaDetails(t));
            row.add(card);
        }

        // Row scroll
        JScrollPane rowScroll = buildRowScroll(row, visibleCount);
        wrap.add(buildScrollWithArrows(rowScroll, row, visibleCount), BorderLayout.CENTER);
        return wrap;
    }

    private List<String> posterFiles() {
        //  discover all poster images
        List<String> list = ImageUtils.listPosterFiles();
        if (list == null || list.isEmpty()) {
            // Fallback  avoid an empty UI if discovery fails
            list = new ArrayList<>();
            list.add("Avatar.jpg");
            list.add("TheLionKing.jpg");
            list.add("Inception.jpg");
        }
        return list;
    }

    private String stripExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private List<String> mockTitles(String prefix, int count) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) list.add(prefix + " Title " + i);
        return list;
    }

    // scroll helpers left/right arrows

    private JScrollPane buildRowScroll(JPanel row, int visibleCount) {
        // Hide inner scrollbars; use arrow buttons instead
        JScrollPane rowScroll = new JScrollPane(row, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rowScroll.setBorder(null);
        rowScroll.setOpaque(false);
        rowScroll.getViewport().setOpaque(false);
        rowScroll.getHorizontalScrollBar().setUnitIncrement(16);
        // Size viewport to show exactly 'visibleCount' cards
        int step = computeCardStepFromRow(row);
        int gap = 12;
        if (row.getLayout() instanceof FlowLayout) gap = ((FlowLayout) row.getLayout()).getHgap();
        int visibleW = Math.max(50, step * visibleCount - gap);
        int prefH = Math.max(row.getPreferredSize().height + 8, 260);
        Dimension viewDim = new Dimension(visibleW, prefH);
        rowScroll.setPreferredSize(viewDim);
        rowScroll.setMinimumSize(viewDim);
        rowScroll.setMaximumSize(viewDim);
        // Shift + mouse wheel scrolls horizontally
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

        // Create arrows first so we can size the container to their actual widths
        JButton left = makeArrowButton("◄");
        JButton right = makeArrowButton("►");

        //  intended center viewport width )
        int step = computeCardStepFromRow(row);
        int gap = 12;
        if (row.getLayout() instanceof FlowLayout) gap = ((FlowLayout) row.getLayout()).getHgap();
        int visibleW = Math.max(50, step * visibleCount - gap);

        // Measure arrow button widths and add small gutters so cards never touch buttons
        int gutter = 8;
        int leftW = left.getPreferredSize().width + gutter;
        int rightW = right.getPreferredSize().width + gutter;

        int overlayReserve = 16; // matches overlay scrollbar width (approx) + a little gap

        // Fix container width so BoxLayout doesn't stretch it
        Dimension contSize = new Dimension(visibleW + leftW + rightW + overlayReserve, rowScroll.getPreferredSize().height);
        container.setPreferredSize(contSize);
        container.setMinimumSize(contSize);
        container.setMaximumSize(contSize);

        // Add the scroll  after sizing decisions
        container.add(rowScroll, BorderLayout.CENTER);

        // Move exactly one card per click
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

        // Update buttons when scrolling
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

    // One-card step
    private int computeCardStep(JScrollPane rowScroll) {
        Component view = rowScroll.getViewport().getView();
        if (view instanceof JPanel) {
            JPanel row = (JPanel) view;
            int gap = 12; // our FlowLayout hgap
            if (row.getLayout() instanceof FlowLayout) {
                gap = ((FlowLayout) row.getLayout()).getHgap();
            }
            if (row.getComponentCount() > 0) {
                Component c = row.getComponent(0);
                int w = c.getPreferredSize().width;
                return Math.max(10, w + gap);
            }
            return 160 + gap; // default poster width
        }
        return 172;
    }

    //  step from row before scrollpane exists
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

    //  Vertical scroll bar right side

    private JScrollBar createOverlayVScroll(JScrollPane master) {
        JScrollBar sb = new JScrollBar(JScrollBar.VERTICAL);
        sb.setModel(master.getVerticalScrollBar().getModel());
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(12, 10));
        sb.setUnitIncrement(24);
        sb.setBlockIncrement(240);
        sb.setUI(new MinimalScrollBarUI());
        sb.setBorder(null);
        return sb;
    }

    //  dark track and grey bar for the scrolling
    static class MinimalScrollBarUI extends BasicScrollBarUI {
        private static final Color TRACK = new Color(20, 20, 20, 80);   // lightly dark box
        private static final Color THUMB = new Color(150, 150, 150, 220); // grey bar
        private static final Color THUMB_BORDER = new Color(0, 0, 0, 80);

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TRACK);
            int arc = 10;
            g2.fillRoundRect(trackBounds.x + 2, trackBounds.y + 2,
                    trackBounds.width - 4, trackBounds.height - 4, arc, arc);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;
            if (thumbBounds.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 10;
            g2.setColor(THUMB);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                    thumbBounds.width - 2, thumbBounds.height - 2, arc, arc);
            g2.setColor(THUMB_BORDER);
            g2.drawRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                    thumbBounds.width - 2, thumbBounds.height - 2, arc, arc);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton() {
            JButton b = new JButton();
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusable(false);
            b.setBorder(null);
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override protected Dimension getMinimumThumbSize() { return new Dimension(10, 30); }
        @Override protected Dimension getMaximumThumbSize() { return new Dimension(10, Integer.MAX_VALUE); }
    }
}
