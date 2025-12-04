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
    private JLabel activeValue;
    private JComponent activeRow;

    public viewAccountFrame(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        // Top nav
        add(new NavBar(nav, true), BorderLayout.NORTH);

        // Info card
        SurfacePanel card = new SurfacePanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));
        card.setPreferredSize(new Dimension(720, 320));
        card.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));

        // Rows with value labels (filled by refreshData)
        usernameValue = createValueLabel();
        fullNameValue = createValueLabel();
        emailValue = createValueLabel();
        addressValue = createValueLabel();
        phoneValue = createValueLabel();
        activeValue = createValueLabel();

        card.add(buildRow("Username", usernameValue));
        card.add(buildRow("Full Name", fullNameValue));
        card.add(buildRow("Email", emailValue));
        card.add(buildRow("Address", addressValue));
        card.add(buildRow("Phone", phoneValue));
        activeRow = buildRow("Active", activeValue);
        card.add(activeRow);

        card.add(Box.createVerticalStrut(20));
        RoundedButton backBtn = new RoundedButton("Back");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> nav.showMemberHome());
        card.add(backBtn);

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

    private void setPlaceholders() {
        if (usernameValue != null) usernameValue.setText("-");
        if (fullNameValue != null) fullNameValue.setText("-");
        if (emailValue != null) emailValue.setText("-");
        if (addressValue != null) addressValue.setText("-");
        if (phoneValue != null) phoneValue.setText("-");
        if (activeValue != null) activeValue.setText("-");
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

            if (admin) {
                if (activeRow != null) activeRow.setVisible(false); // hide Active for admin
            } else {
                if (activeRow != null) activeRow.setVisible(true);
                boolean active = BackendService.isMemberActive(current);
                activeValue.setText(active ? "Yes" : "No");
            }
        } catch (Exception ignored) {}
    }
}

