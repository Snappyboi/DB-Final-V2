package frontend.admin;

import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import java.awt.*;

// Admin remove media page
public class AdminRemoveMediaPage extends JPanel {
    private final Navigation nav;
    private JTextField idOrTitleField;

    public AdminRemoveMediaPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Remove Media");
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

        idOrTitleField = new JTextField(28);
        styleField(idOrTitleField);

        JLabel l = new JLabel("Media ID or Title:");
        l.setForeground(AdminTheme.TEXT_SECONDARY);
        l.setFont(AdminTheme.fontBold(14));
        gc.gridx = 0; gc.gridy = 0; card.add(l, gc);
        gc.gridx = 1; card.add(idOrTitleField, gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        RoundedButton remove = new RoundedButton("Remove").red();
        remove.addActionListener(e -> submitRemove());
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

    private void styleField(JTextField f) {
        f.setBackground(AdminTheme.SURFACE);
        f.setForeground(AdminTheme.TEXT_PRIMARY);
        f.setCaretColor(AdminTheme.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.GRID, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        f.setFont(AdminTheme.fontRegular(14));
    }

    private void submitRemove() {
        String key = idOrTitleField.getText() == null ? "" : idOrTitleField.getText().trim();
        if (key.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter an ID or Title."); return; }
        boolean ok = backend.BackendService.removeMediaByIdOrTitle(key);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Media removed.");
            idOrTitleField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Media not found or could not be removed.");
        }
    }
}
