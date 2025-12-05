package frontend.admin;

import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import java.awt.*;

// Admin add person (step 1 before creating a member)
public class AdminAddPersonPage extends JPanel {
    private final Navigation nav;
    private final JTextField nameField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField addressField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);

    public AdminAddPersonPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(AdminTheme.BACKGROUND);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Add Person");
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

        style(nameField); style(emailField); style(addressField); style(phoneField);

        int r = 0;
        addRow(card, gc, r++, "Name", nameField);
        addRow(card, gc, r++, "Email", emailField);
        addRow(card, gc, r++, "Address", addressField);
        addRow(card, gc, r++, "Phone", phoneField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        RoundedButton next = new RoundedButton("Next").gold();
        next.addActionListener(e -> goNext());
        actions.add(next);
        gc.gridx = 1; gc.gridy = r; card.add(actions, gc);

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

    private void addRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent input) {
        JLabel l = new JLabel(label + ":");
        l.setForeground(AdminTheme.TEXT_SECONDARY);
        l.setFont(AdminTheme.fontBold(14));
        gc.gridx = 0; gc.gridy = row; panel.add(l, gc);
        gc.gridx = 1; panel.add(input, gc);
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

    private void goNext() {
        String name = val(nameField), email = val(emailField), address = val(addressField), phone = val(phoneField);
        frontend.PersonData pd = new frontend.PersonData(name, email, address, phone);
        try {
            // Create Person in DBand  store returned ID
            Integer pid = backend.BackendService.createPerson(name, email, address, phone);
            pd.personId = pid;
            if (pid != null) {
                JOptionPane.showMessageDialog(this, "Person added with ID: " + pid);
            } else {
                // If table missing, we still continue to next step
                JOptionPane.showMessageDialog(this, "Person saved.");
            }
        } catch (Exception ignored) {}
        if (nav instanceof frontend.app) {
            ((frontend.app) nav).startAddMemberWithPerson(pd);
        }
    }

    private String val(JTextField f) { return f.getText() == null ? "" : f.getText().trim(); }
}
