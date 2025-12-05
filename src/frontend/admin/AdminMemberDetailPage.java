package frontend.admin;

import backend.BackendService;
import frontend.Navigation;
import frontend.Theme;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;
import frontend.components.TableStyler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

// Admin member detail page
public class AdminMemberDetailPage extends JPanel {
    private final Navigation nav;
    private JLabel usernameValue, nameValue, emailValue, addressValue, phoneValue, subValue, activeValue;
    private JTable historyTable;

    public AdminMemberDetailPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(AdminTheme.BACKGROUND);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Member Details");
        title.setForeground(AdminTheme.TEXT_PRIMARY);
        title.setFont(AdminTheme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        // Back row
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        backRow.setOpaque(false);
        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> {
            if (nav instanceof frontend.app) ((frontend.app) nav).showAdminSubscriptions(); else nav.showAdminHome();
        });
        backRow.add(back);

        // Info card
        SurfacePanel infoCard = new SurfacePanel();
        infoCard.setBackground(AdminTheme.SURFACE_ELEVATED);
        infoCard.setLayout(new GridBagLayout());
        infoCard.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST; gc.insets = new Insets(4,4,4,24);

        usernameValue = makeValue();
        nameValue = makeValue();
        emailValue = makeValue();
        addressValue = makeValue();
        phoneValue = makeValue();
        subValue = makeValue();
        activeValue = makeValue();

        addRow(infoCard, gc, "Username", usernameValue);
        addRow(infoCard, gc, "Name", nameValue);
        addRow(infoCard, gc, "Email", emailValue);
        addRow(infoCard, gc, "Address", addressValue);
        addRow(infoCard, gc, "Phone", phoneValue);
        addRow(infoCard, gc, "Subscription", subValue);
        addRow(infoCard, gc, "Active", activeValue);

        // Watch history card
        String[] cols = {"Title", "Watched"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){ return false; } };
        historyTable = new JTable(model);
        TableStyler.applyAdminStyle(historyTable);

        SurfacePanel historyCard = new SurfacePanel();
        historyCard.setBackground(AdminTheme.SURFACE_ELEVATED);
        historyCard.setLayout(new BorderLayout());
        JLabel histTitle = new JLabel("Watch History");
        histTitle.setForeground(AdminTheme.TEXT_PRIMARY);
        histTitle.setFont(AdminTheme.fontBold(16));
        histTitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        JPanel histWrap = new JPanel(new BorderLayout());
        histWrap.setOpaque(false);
        histWrap.add(histTitle, BorderLayout.NORTH);
        historyCard.add(histWrap, BorderLayout.NORTH);
        historyCard.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        historyCard.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setOpaque(false);
        northWrap.add(title, BorderLayout.NORTH);
        northWrap.add(backRow, BorderLayout.CENTER);
        add(northWrap, BorderLayout.NORTH);
        center.add(infoCard);
        center.add(Box.createVerticalStrut(8));
        center.add(historyCard);
        add(center, BorderLayout.CENTER);
    }

    private JLabel makeValue() {
        JLabel v = new JLabel("-");
        v.setForeground(Theme.TEXT_PRIMARY);
        v.setFont(Theme.fontRegular(14));
        return v;
    }

    private void addRow(JPanel panel, GridBagConstraints gc, String label, JLabel value) {
        JLabel l = new JLabel(label + ":");
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setFont(Theme.fontBold(14));
        gc.gridx = 0; panel.add(l, gc);
        gc.gridx = 1; panel.add(value, gc);
        gc.gridy++;
    }

    // Load info + history for a username
    public void showUser(String username) {
        // Info
        try {
            Map<String, String> info = BackendService.getMemberDetail(username);
            usernameValue.setText(orDash(info.get("username")));
            nameValue.setText(orDash(info.get("fullName")));
            emailValue.setText(orDash(info.get("email")));
            addressValue.setText(orDash(info.get("address")));
            phoneValue.setText(orDash(info.get("phone")));
            subValue.setText(orDash(info.get("subscription_level")));
            String active = info.get("active");
            activeValue.setText(orDash(active));
        } catch (Exception ignored) {
            usernameValue.setText(orDash(username));
        }
        // History
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);
        try {
            List<String[]> rows = BackendService.getWatchHistoryRowsByUsername(username);
            if (rows != null) {
                for (String[] r : rows) {
                    String title = r != null && r.length > 0 ? r[0] : "-";
                    String when = r != null && r.length > 1 ? r[1] : "-";
                    model.addRow(new Object[]{orDash(title), orDash(when)});
                }
            }
        } catch (Exception ignored) {}
    }

    private String orDash(String s) { return (s == null || s.isBlank()) ? "-" : s; }
}
