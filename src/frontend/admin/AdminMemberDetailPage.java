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
    private JComboBox<String> subBox;
    private JTable historyTable;
    private JTextField usernameField, nameField, emailField, addressField, phoneField;
    private JButton editButton, saveButton, cancelButton;
    private boolean editing = false;

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
        subBox = new JComboBox<>(new String[]{"Basic", "Premium"});
        styleCombo(subBox);
        activeValue = makeValue();

        // Editors
        usernameField = new JTextField(22);
        nameField = new JTextField(22);
        emailField = new JTextField(22);
        addressField = new JTextField(22);
        phoneField = new JTextField(22);

        addRow(infoCard, gc, "Username", usernameValue, usernameField);
        addRow(infoCard, gc, "Name", nameValue, nameField);
        addRow(infoCard, gc, "Email", emailValue, emailField);
        addRow(infoCard, gc, "Address", addressValue, addressField);
        addRow(infoCard, gc, "Phone", phoneValue, phoneField);
        addRow(infoCard, gc, "Subscription", subValue);

        // Editable subscription row
        JLabel editLabel = new JLabel("Change Plan:");
        editLabel.setForeground(Theme.TEXT_SECONDARY);
        editLabel.setFont(Theme.fontBold(14));
        gc.gridx = 0; infoCard.add(editLabel, gc);
        gc.gridx = 1; infoCard.add(subBox, gc);
        gc.gridy++;

        // Save button row (subscription only)
        RoundedButton saveSub = new RoundedButton("Save Subscription").gold();
        saveSub.addActionListener(e -> saveSubscription());
        gc.gridx = 1; infoCard.add(saveSub, gc);
        gc.gridy++;
        addRow(infoCard, gc, "Active", activeValue);

        // Edit controls for member fields
        gc.gridx = 0; infoCard.add(Box.createVerticalStrut(6), gc); gc.gridy++;
        JPanel editButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editButtons.setOpaque(false);
        editButton = new RoundedButton("Edit");
        saveButton = new RoundedButton("Save");
        cancelButton = new RoundedButton("Cancel");
        editButton.addActionListener(e -> setEditing(true));
        cancelButton.addActionListener(e -> { setEditing(false); /* restore from labels */
            usernameField.setText(textOf(usernameValue));
            nameField.setText(textOf(nameValue));
            emailField.setText(textOf(emailValue));
            addressField.setText(textOf(addressValue));
            phoneField.setText(textOf(phoneValue));
        });
        saveButton.addActionListener(e -> saveMemberEdits());
        editButtons.add(editButton);
        editButtons.add(saveButton);
        editButtons.add(cancelButton);
        gc.gridx = 1; infoCard.add(editButtons, gc);
        gc.gridy++;

        // Watch history card
        String[] cols = {"Title", "Watched"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){
            return false;
        }
        };
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

    private void addRow(JPanel panel, GridBagConstraints gc, String label, JLabel value, JComponent editor) {
        JLabel l = new JLabel(label + ":");
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setFont(Theme.fontBold(14));
        gc.gridx = 0; panel.add(l, gc);

        JPanel right = new JPanel(new CardLayout());
        right.setOpaque(false);
        right.add(value, "view");
        right.add(editor, "edit");
        right.putClientProperty("rightCard", right);
        gc.gridx = 1; panel.add(right, gc);
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
            // Fill editors
            usernameField.setText(textOf(usernameValue));
            nameField.setText(textOf(nameValue));
            emailField.setText(textOf(emailValue));
            addressField.setText(textOf(addressValue));
            phoneField.setText(textOf(phoneValue));
            setEditing(false);
            String currentSub = info.get("subscription_level");
            if (currentSub != null) {
                if (currentSub.equalsIgnoreCase("premium")) subBox.setSelectedItem("Premium");
                else subBox.setSelectedItem("Basic");
            } else {
                subBox.setSelectedItem("Basic");
            }
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

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setFont(AdminTheme.fontRegular(14));
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1));

        // White dropdown with black text
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(AdminTheme.fontRegular(14));
                if (isSelected) {
                    setBackground(new Color(240,240,240));
                    setForeground(Color.BLACK);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    private void saveSubscription() {
        String username = usernameValue.getText();
        if (username == null || username.isBlank() || username.equals("-")) {
            JOptionPane.showMessageDialog(this, "No member loaded.");
            return;
        }
        String level = (String) subBox.getSelectedItem();
        boolean ok = BackendService.updateMemberSubscription(username, level);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Subscription updated.");
            showUser(username);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update subscription.");
        }
    }

    private void setEditing(boolean edit) {
        this.editing = edit;
        // Switch all rows that have a rightCard
        Container parent = this;
        // Traverse only info card children by toggling CardLayouts we created
        toggleCards(this, edit);
        if (editButton != null) editButton.setEnabled(!edit);
        if (saveButton != null) saveButton.setEnabled(edit);
        if (cancelButton != null) cancelButton.setEnabled(edit);
        revalidate(); repaint();
    }

    private void toggleCards(Container root, boolean edit) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                Object right = p.getClientProperty("rightCard");
                if (right instanceof JPanel) {
                    CardLayout cl = (CardLayout) ((JPanel) right).getLayout();
                    cl.show((JPanel) right, edit ? "edit" : "view");
                }
                toggleCards(p, edit);
            }
        }
    }

    private String textOf(JLabel l) {
        String t = l.getText();
        return (t == null || t.equals("-")) ? "" : t;
    }

    private void saveMemberEdits() {
        String currentUsername = usernameValue.getText();
        if (currentUsername == null || currentUsername.isBlank() || currentUsername.equals("-")) {
            JOptionPane.showMessageDialog(this, "No member loaded.");
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newAddress = addressField.getText().trim();
        String newPhone = phoneField.getText().trim();

        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        boolean ok = BackendService.updateMemberProfile(
                currentUsername,
                newUsername,
                newName,
                newEmail,
                newAddress,
                newPhone,
                null // password unchanged
        );

        if (!ok) {
            JOptionPane.showMessageDialog(this, "Save failed. Username may already be taken or no changes provided.");
            return;
        }

        // Update labels
        usernameValue.setText(newUsername);
        nameValue.setText(orDash(newName));
        emailValue.setText(orDash(newEmail));
        addressValue.setText(orDash(newAddress));
        phoneValue.setText(orDash(newPhone));

        setEditing(false);
        JOptionPane.showMessageDialog(this, "Member details saved.");
    }
}
