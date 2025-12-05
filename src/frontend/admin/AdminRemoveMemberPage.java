package frontend.admin;

import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import java.awt.*;

// Admin remove member page
public class AdminRemoveMemberPage extends JPanel {
    private final Navigation nav;
    private JTextField userField;

    public AdminRemoveMemberPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Remove Member");
        title.setForeground(AdminTheme.TEXT_PRIMARY);
        title.setFont(AdminTheme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        backRow.setOpaque(false);
        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> nav.showAdminHome());
        backRow.add(back);

        SurfacePanel card = new SurfacePanel();
        card.setBackground(AdminTheme.SURFACE_ELEVATED);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        userField = new JTextField(24);
        style(userField);
        JLabel l = new JLabel("Username:");
        l.setForeground(AdminTheme.TEXT_SECONDARY);
        l.setFont(AdminTheme.fontBold(14));
        gc.gridx = 0; gc.gridy = 0; card.add(l, gc);
        gc.gridx = 1; card.add(userField, gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        RoundedButton remove = new RoundedButton("Remove Member").red();
        remove.addActionListener(e -> submit());
        actions.add(remove);
        gc.gridx = 1; gc.gridy = 1; card.add(actions, gc);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setOpaque(false);
        northWrap.add(title, BorderLayout.NORTH);
        northWrap.add(backRow, BorderLayout.CENTER);
        center.add(northWrap, BorderLayout.NORTH);
        center.add(card, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void style(JTextField f) {
        f.setBackground(AdminTheme.SURFACE);
        f.setForeground(AdminTheme.TEXT_PRIMARY);
        f.setCaretColor(AdminTheme.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.GRID, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        f.setFont(AdminTheme.fontRegular(14));
    }

    private void submit() {
        String u = userField.getText() == null ? "" : userField.getText().trim();
        if (u.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a username."); return; }
        boolean ok = backend.BackendService.removeMemberByUsername(u);
        if (ok) { JOptionPane.showMessageDialog(this, "Member removed."); userField.setText(""); }
        else { JOptionPane.showMessageDialog(this, "User not found or could not be removed."); }
    }
}
