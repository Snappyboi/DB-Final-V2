package frontend.admin;

import backend.BackendService;
import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.TableStyler;
import frontend.components.SurfacePanel;
import frontend.components.RoundedButton;

import javax.swing.*;
import java.awt.*;

// Admin member streaming page
public class AdminMemberStreaming extends JPanel {
    private final Navigation nav;
    private final JTextField searchField = new JTextField(28);
    private final JTable table;

    public AdminMemberStreaming(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Member Streaming (Admin)");
        title.setForeground(AdminTheme.TEXT_PRIMARY);
        title.setFont(AdminTheme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        // Back button
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        backRow.setOpaque(false);
        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> nav.showAdminHome());
        backRow.add(back);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        searchRow.setOpaque(false);
        styleField(searchField);
        RoundedButton btn = new RoundedButton("Search").gold();
        btn.addActionListener(e -> doSearch());
        searchRow.add(searchField);
        searchRow.add(btn);

        String[] cols = {"Member", "Media", "Last Watched"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        TableStyler.applyAdminStyle(table);

        // Open member detail on double-click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && nav instanceof frontend.app) {
                        String username = String.valueOf(table.getValueAt(row, 0));
                        ((frontend.app) nav).showAdminMemberDetail(username);
                    }
                }
            }
        });

        SurfacePanel card = new SurfacePanel();
        card.setBackground(AdminTheme.SURFACE_ELEVATED);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setOpaque(false);
        northWrap.add(title, BorderLayout.NORTH);
        northWrap.add(backRow, BorderLayout.CENTER);
        center.add(northWrap, BorderLayout.NORTH);
        center.add(searchRow, BorderLayout.CENTER);
        center.add(card, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    private void styleField(JTextField f) {
        f.setBackground(AdminTheme.SURFACE);
        f.setForeground(AdminTheme.TEXT_PRIMARY);
        f.setCaretColor(AdminTheme.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.GRID, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        f.setFont(AdminTheme.fontRegular(14));
    }

    private void doSearch() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim();
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) table.getModel();
        model.setRowCount(0);
        try {
            java.util.List<String[]> rows = BackendService.searchMemberStreaming(q);
            if (rows == null || rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No results.");
                return;
            }
            for (String[] r : rows) {
                String user = r != null && r.length > 0 ? r[0] : "-";
                String title = r != null && r.length > 1 ? r[1] : "-";
                String when = r != null && r.length > 2 ? r[2] : "-";
                model.addRow(new Object[]{user, title, when});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search error.");
        }
    }
}
