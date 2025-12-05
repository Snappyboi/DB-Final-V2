package frontend.admin;

import backend.BackendService;
import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;
import frontend.components.TableStyler;
import frontend.components.AdminTableRenderers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

// Admin subscriptions page
public class AdminSubscriptionsPage extends JPanel {
    private final Navigation nav;
    private JTable table;

    public AdminSubscriptionsPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Subscriptions");
        title.setForeground(AdminTheme.TEXT_PRIMARY);
        title.setFont(AdminTheme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        // Back button
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        backRow.setOpaque(false);
        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> nav.showAdminHome());
        backRow.add(back);

        String[] cols = {"Name", "Username", "Subscription", "Active"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        TableStyler.applyAdminStyle(table);
        table.getColumnModel().getColumn(0).setCellRenderer(AdminTableRenderers.avatarWithNameRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(AdminTableRenderers.avatarWithNameRenderer());

        // open member detail when double-click on a row
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && nav instanceof frontend.app) {
                        String username = String.valueOf(table.getValueAt(row, 1));
                        ((frontend.app) nav).showAdminMemberDetail(username);
                    }
                }
            }
        });

        SurfacePanel card = new SurfacePanel();
        card.setBackground(AdminTheme.SURFACE_ELEVATED);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));
        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        card.add(sp, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setOpaque(false);
        northWrap.add(title, BorderLayout.NORTH);
        northWrap.add(backRow, BorderLayout.CENTER);
        center.add(northWrap, BorderLayout.NORTH);
        center.add(card, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        reload();
    }

    public void reload() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        try {
            List<Map<String, String>> list = BackendService.listMembersWithSubscription();
            if (list == null || list.isEmpty()) {
                return;
            }
            for (Map<String, String> m : list) {
                String name = orDash(m.get("member_name"));
                String user = orDash(m.get("username"));
                String sub = orDash(m.get("subscription_level"));
                String active = orDash(m.get("active"));
                model.addRow(new Object[]{name, user, sub, active});
            }
        } catch (Exception ignored) {}
    }

    private String orDash(String s) { return (s == null || s.isBlank()) ? "-" : s; }
}
