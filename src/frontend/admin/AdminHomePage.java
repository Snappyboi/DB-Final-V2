package frontend.admin;

import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminHomePage extends JPanel {
    private final Navigation nav;
    private JTable table;

    public AdminHomePage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        toolbar.setBackground(AdminTheme.SURFACE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        RoundedButton addMedia = new RoundedButton("Add Media").gold();
        RoundedButton removeMedia = new RoundedButton("Remove Media");
        RoundedButton addMember = new RoundedButton("Add Member").gold();
        RoundedButton removeMember = new RoundedButton("Remove Member");
        RoundedButton trends = new RoundedButton("View Trends");
        RoundedButton memberStreaming = new RoundedButton("Member Streaming");
        RoundedButton subscriptions = new RoundedButton("Subscriptions");

        addMedia.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminAddMedia();
            } else {
                JOptionPane.showMessageDialog(this, "Add Media not available.");
            }
        });
        removeMedia.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminRemoveMedia();
            } else {
                JOptionPane.showMessageDialog(this, "Remove Media not available.");
            }
        });
        addMember.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminAddPerson();
            } else {
                JOptionPane.showMessageDialog(this, "Add Member not available.");
            }
        });
        removeMember.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminRemoveMember();
            } else {
                JOptionPane.showMessageDialog(this, "Remove Member not available.");
            }
        });
        trends.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminAnalytics();
            } else {
                JOptionPane.showMessageDialog(this, "Admin analytics not available.");
            }
        });
        memberStreaming.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminMemberStreaming();
            } else {
                JOptionPane.showMessageDialog(this, "Member streaming not available.");
            }
        });
        subscriptions.addActionListener(e -> {
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminSubscriptions();
            } else {
                JOptionPane.showMessageDialog(this, "Subscriptions page not available.");
            }
        });

        toolbar.add(addMedia);
        toolbar.add(removeMedia);
        toolbar.add(addMember);
        toolbar.add(removeMember);
        toolbar.add(trends);
        toolbar.add(memberStreaming);
        toolbar.add(subscriptions);
        add(toolbar, BorderLayout.SOUTH);

        String[] cols = {"Type", "Title/Name", "Status", "Updated"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){ return false; } };
        table = new JTable(model);
        frontend.components.TableStyler.applyAdminStyle(table);

        frontend.components.SurfacePanel card = new frontend.components.SurfacePanel();
        card.setBackground(AdminTheme.SURFACE_ELEVATED);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        reload();
    }

    public void reload() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        try {
            java.util.List<String[]> rows = backend.BackendService.getRecentActivity(100);
            if (rows != null) {
                for (String[] r : rows) {
                    String type = r.length > 0 ? r[0] : "-";
                    String name = r.length > 1 ? r[1] : "-";
                    String status = r.length > 2 ? r[2] : "-";
                    String when = r.length > 3 ? r[3] : "-";
                    model.addRow(new Object[]{type, name, status, when});
                }
            }
        } catch (Exception ignored) {}

    }
}
