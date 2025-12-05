// Account info page (DB_Final name: viewAccountFrame)
package frontend;

import backend.BackendService;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class viewAccountFrame extends JPanel {
    private final Navigation nav;
    private JLabel usernameValue;
    private JLabel fullNameValue;
    private JLabel emailValue;
    private JLabel addressValue;
    private JLabel phoneValue;

    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField phoneField;
    private JPasswordField passwordField;

    private JPanel card;
    private boolean editing = false;
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;

    public viewAccountFrame(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        // Top nav
        add(new NavBar(nav, true), BorderLayout.NORTH);

        // Info card
        card = new SurfacePanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));
        card.setPreferredSize(new Dimension(720, 380));
        card.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));

        // Rows with value labels (filled by refreshData)
        usernameValue = createValueLabel();
        fullNameValue = createValueLabel();
        emailValue = createValueLabel();
        addressValue = createValueLabel();
        phoneValue = createValueLabel();

        // Editable fields
        usernameField = new JTextField();
        fullNameField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();
        phoneField = new JTextField();
        passwordField = new JPasswordField();
        Dimension fieldDim = new Dimension(360, 28);
        for (JComponent f : new JComponent[]{usernameField, fullNameField, emailField, addressField, phoneField, passwordField}) {
            f.setMaximumSize(fieldDim);
        }

        card.add(buildRow("Username", usernameValue, usernameField));
        card.add(buildRow("Full Name", fullNameValue, fullNameField));
        card.add(buildRow("Email", emailValue, emailField));
        card.add(buildRow("Address", addressValue, addressField));
        card.add(buildRow("Phone", phoneValue, phoneField));
        card.add(buildRow("New Password", new JLabel("••••••"), passwordField));

        card.add(Box.createVerticalStrut(16));
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));

        editButton = new RoundedButton("Edit");
        saveButton = new RoundedButton("Save");
        cancelButton = new RoundedButton("Cancel");

        editButton.addActionListener(e -> setEditing(true));
        cancelButton.addActionListener(e -> { setEditing(false); refreshData(); });
        saveButton.addActionListener(e -> onSave());

        buttons.add(editButton);
        buttons.add(saveButton);
        buttons.add(cancelButton);

        RoundedButton backBtn = new RoundedButton("Back");
        backBtn.addActionListener(e -> {
            if (nav instanceof AdminAware && ((AdminAware) nav).isAdmin()) nav.showAdminHome(); else nav.showMemberHome();
        });
        buttons.add(backBtn);
        card.add(buttons);

        // Center: title + card
        JLabel title = new JLabel("Account Information");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.fontBold(28));
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(card);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(title, BorderLayout.NORTH);
        content.add(centerWrap, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        setPlaceholders();
    }

    private JLabel createValueLabel() {
        JLabel value = new JLabel("Loading…");
        value.setFont(Theme.fontRegular(14));
        value.setForeground(Theme.TEXT_PRIMARY);
        value.setHorizontalAlignment(SwingConstants.LEFT);
        value.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        return value;
    }

    private JComponent buildRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel left = new JLabel(label + ":");
        left.setFont(Theme.fontBold(14));
        left.setForeground(Theme.TEXT_SECONDARY);
        Dimension d = left.getPreferredSize();
        left.setPreferredSize(new Dimension(140, d != null ? d.height : 20));
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        row.add(left, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JComponent buildRow(String label, JLabel valueLabel, JComponent editorField) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel left = new JLabel(label + ":");
        left.setFont(Theme.fontBold(14));
        left.setForeground(Theme.TEXT_SECONDARY);
        Dimension d = left.getPreferredSize();
        left.setPreferredSize(new Dimension(140, d != null ? d.height : 20));
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new CardLayout());
        right.add(valueLabel, "view");
        right.add(editorField, "edit");

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        row.putClientProperty("rightCard", right);
        return row;
    }

    private void setPlaceholders() {
        if (usernameValue != null) usernameValue.setText("-");
        if (fullNameValue != null) fullNameValue.setText("-");
        if (emailValue != null) emailValue.setText("-");
        if (addressValue != null) addressValue.setText("-");
        if (phoneValue != null) phoneValue.setText("-");
    }

    // Refresh account information from the database for the currently logged-in user.
    public void refreshData() {
        try {
            String current = nav.getCurrentUsername();
            if (current == null || current.isEmpty()) {
                setPlaceholders();
                return;
            }
            boolean admin = (nav instanceof AdminAware) && ((AdminAware) nav).isAdmin();
            java.util.Map<String, String> info = admin
                    ? BackendService.getAdminAccountInfo(current)
                    : BackendService.getMemberAccountInfo(current);

            String uName   = info.getOrDefault("username", current);
            String full    = info.getOrDefault("fullName", "Unknown");
            String email   = info.getOrDefault("email", "Unknown");
            String address = info.getOrDefault("address", "Unknown");
            String phone   = info.getOrDefault("phone", "Unknown");

            usernameValue.setText(uName);
            fullNameValue.setText(full);
            emailValue.setText(email);
            addressValue.setText(address);
            phoneValue.setText(phone);

            // Fill editors
            usernameField.setText(uName);
            fullNameField.setText(full);
            emailField.setText(email);
            addressField.setText(address);
            phoneField.setText(phone);
            passwordField.setText("");

            setEditing(false);
        } catch (Exception ignored) {}
    }

    private void setEditing(boolean edit) {
        this.editing = edit;
        // Switch all rows to view/edit
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) {
                JPanel row = (JPanel) c;
                Object right = row.getClientProperty("rightCard");
                if (right instanceof JPanel) {
                    CardLayout cl = (CardLayout) ((JPanel) right).getLayout();
                    cl.show((JPanel) right, edit ? "edit" : "view");
                }
            }
        }
        editButton.setEnabled(!edit);
        saveButton.setEnabled(edit);
        cancelButton.setEnabled(edit);
    }

    private void onSave() {
        try {
            boolean isAdmin = (nav instanceof AdminAware) && ((AdminAware) nav).isAdmin();
            String current = nav.getCurrentUsername();

            String newUsername = usernameField.getText().trim();
            String name = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok;
            if (isAdmin) {
                ok = BackendService.updateAdminProfile(current, newUsername, name, email, address, phone, password.isEmpty() ? null : password);
            } else {
                ok = BackendService.updateMemberProfile(current, newUsername, name, email, address, phone, password.isEmpty() ? null : password);
            }

            if (!ok) {
                JOptionPane.showMessageDialog(this, "Save failed. Username may already be taken or no changes provided.", "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update session username if changed
            if (!newUsername.equals(current)) {
                nav.setCurrentUsername(newUsername);
            }

            setEditing(false);
            refreshData();
            JOptionPane.showMessageDialog(this, "Changes saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving changes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

