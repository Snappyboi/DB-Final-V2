package frontend.admin;

import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import java.awt.*;

// Admin add member page (Step 2) after adding person (doesnt work)
public class AdminAddMemberPage extends JPanel {
    private final Navigation nav;
    private JTextField usernameField, passwordField, nameField, emailField, addressField, phoneField;
    private JComboBox<String> subLevelBox;
    private Integer personId;

    public AdminAddMemberPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Add Member");
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

        usernameField = new JTextField(22);
        passwordField = new JTextField(22);
        nameField = new JTextField(22);
        emailField = new JTextField(22);
        addressField = new JTextField(22);
        phoneField = new JTextField(22);
        subLevelBox = new JComboBox<>(new String[]{"-", "Basic", "Premium"});
        style(usernameField); style(passwordField); style(nameField); style(emailField); style(addressField); style(phoneField);

        int r = 0;
        addRow(card, gc, r++, "Username", usernameField);
        addRow(card, gc, r++, "Password", passwordField);
        addRow(card, gc, r++, "Name", nameField);
        addRow(card, gc, r++, "Email", emailField);
        addRow(card, gc, r++, "Address", addressField);
        addRow(card, gc, r++, "Phone", phoneField);
        addRow(card, gc, r++, "Subscription", subLevelBox);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        RoundedButton add = new RoundedButton("Add Member").gold();
        add.addActionListener(e -> submit());
        actions.add(add);
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

    private void submit() {
        String u = val(usernameField), p = val(passwordField), n = val(nameField), e = val(emailField), a = val(addressField), ph = val(phoneField);
        String sub = (String) subLevelBox.getSelectedItem();
        if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Username and password required."); return; }
        boolean ok;
        if (personId != null) {
            ok = backend.BackendService.createMemberUsingPerson(personId, u, p, n, e, a, ph, sub, false);
        } else {
            ok = backend.BackendService.createMemberFull(u, p, n, e, a, ph, sub, false);
        }
        if (ok) {
            JOptionPane.showMessageDialog(this, "Member added.");
            usernameField.setText(""); passwordField.setText(""); nameField.setText(""); emailField.setText(""); addressField.setText(""); phoneField.setText("");
            subLevelBox.setSelectedIndex(0);
            // Go to subscriptions and reload
            if (nav instanceof frontend.app) {
                ((frontend.app) nav).showAdminSubscriptions();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Could not add member.");
        }
    }

    private String val(JTextField f) { return f.getText() == null ? "" : f.getText().trim(); }

    public void setPersonData(frontend.PersonData pd) {
        if (pd != null) {
            this.personId = pd.personId;
            nameField.setText(pd.name == null ? "" : pd.name);
            emailField.setText(pd.email == null ? "" : pd.email);
            addressField.setText(pd.address == null ? "" : pd.address);
            phoneField.setText(pd.phone == null ? "" : pd.phone);
        }
    }
}
