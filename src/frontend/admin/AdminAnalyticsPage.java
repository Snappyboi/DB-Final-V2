package frontend.admin;

import backend.BackendService;
import backend.Media;
import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.SurfacePanel;
import frontend.components.TableStyler;
import frontend.components.AdminTableRenderers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Admin analytics page
public class AdminAnalyticsPage extends JPanel {
    private final Navigation nav;
    private JTable table;
    private enum Mode { OVERALL, MONTH, LAST24 }
    private Mode mode = Mode.OVERALL;

    public AdminAnalyticsPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Top Media");
        title.setForeground(AdminTheme.TEXT_PRIMARY);
        title.setFont(AdminTheme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        // Back button to return to Admin home
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        backRow.setOpaque(false);
        frontend.components.RoundedButton back = new frontend.components.RoundedButton("Back");
        back.addActionListener(e -> nav.showAdminHome());
        backRow.add(back);

        // Mode buttons
        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modeRow.setOpaque(false);
        frontend.components.RoundedButton overallBtn = new frontend.components.RoundedButton("Trending").gold();
        frontend.components.RoundedButton monthBtn = new frontend.components.RoundedButton("This Month");
        frontend.components.RoundedButton last24Btn = new frontend.components.RoundedButton("Last 24h");
        overallBtn.addActionListener(e -> { mode = Mode.OVERALL; reload(); });
        monthBtn.addActionListener(e -> { mode = Mode.MONTH; reload(); });
        last24Btn.addActionListener(e -> { mode = Mode.LAST24; reload(); });
        modeRow.add(overallBtn);
        modeRow.add(monthBtn);
        modeRow.add(last24Btn);

        //  admin table
        String[] cols = {"ID", "Title", "Release", "Genre"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        TableStyler.applyAdminStyle(table);
        table.getColumnModel().getColumn(1).setCellRenderer(AdminTableRenderers.posterWithTitleRenderer());

        // double-click to open details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) {
                        String titleVal = String.valueOf(table.getValueAt(r, 1));
                        nav.showMediaDetails(titleVal);
                    }
                }
            }
        });

        SurfacePanel panel = new SurfacePanel();
        panel.setBackground(AdminTheme.SURFACE_ELEVATED);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setOpaque(false);
        northWrap.add(title, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setOpaque(false);
        mid.add(backRow, BorderLayout.WEST);
        mid.add(modeRow, BorderLayout.CENTER);
        northWrap.add(mid, BorderLayout.CENTER);
        center.add(northWrap, BorderLayout.NORTH);
        center.add(panel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        reload();
    }

    public void reload() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        try {
            List<Media> list;
            switch (mode) {
                case MONTH: list = BackendService.getTopMediaThisMonth(10); break;
                case LAST24: list = BackendService.getTrendingLast24h(10); break;
                case OVERALL:
                default: list = BackendService.getTrendingMedia(10); break;
            }
            if (list != null) {
                for (Media m : list) {
                    String raw = null;
                    try { raw = m.getMediaIdRaw(); } catch (Exception ignored) {}
                    if (raw == null || raw.isBlank()) {
                        int n = 0; try { n = m.getMediaID(); } catch (Exception ignored) {}
                        raw = String.format("M%03d", n);
                    }
                    String title = m.getTitle();
                    String release = m.getReleaseDate();
                    String genre = m.getGenre();
                    model.addRow(new Object[]{raw, title, release, genre});
                }
            }
        } catch (Exception ignored) {}
    }
}
