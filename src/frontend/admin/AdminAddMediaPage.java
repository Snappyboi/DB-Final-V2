package frontend.admin;

import frontend.Navigation;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import java.awt.*;

// Admin add media page
public class AdminAddMediaPage extends JPanel {
    private final Navigation nav;
    private JTextField titleField, genreField, releaseField, imdbField;
    private JComboBox<String> typeBox;

    public AdminAddMediaPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(AdminTheme.BACKGROUND);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JLabel title = new JLabel("Add Media");
        title.setForeground(AdminTheme.TEXT_PRIMARY);
        title.setFont(AdminTheme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        // Back button
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        backRow.setOpaque(false);
        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> nav.showAdminHome());
        backRow.add(back);

        // Form
        SurfacePanel card = new SurfacePanel();
        card.setBackground(AdminTheme.SURFACE_ELEVATED);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        typeBox = new JComboBox<>(new String[]{"Movie", "Series"});
        titleField = new JTextField(24);
        genreField = new JTextField(24);
        releaseField = new JTextField(24);
        imdbField = new JTextField(24);
        styleField(titleField); styleField(genreField); styleField(releaseField); styleField(imdbField);

        int r = 0;
        addRow(card, gc, r++, "Type", typeBox);
        addRow(card, gc, r++, "Title", titleField);
        addRow(card, gc, r++, "Genre", genreField);
        addRow(card, gc, r++, "Release", releaseField);
        addRow(card, gc, r++, "IMBD Link", imdbField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        RoundedButton save = new RoundedButton("Add").gold();
        save.addActionListener(e -> submit());
        actions.add(save);
        gc.gridx = 1; gc.gridy = r; gc.gridwidth = 1; card.add(actions, gc);

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

    private void addRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent comp) {
        JLabel l = new JLabel(label + ":");
        l.setForeground(AdminTheme.TEXT_SECONDARY);
        l.setFont(AdminTheme.fontBold(14));
        gc.gridx = 0; gc.gridy = row; panel.add(l, gc);
        gc.gridx = 1; panel.add(comp, gc);
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

    private void submit() {
        String type = (String) typeBox.getSelectedItem();
        String title = safe(titleField.getText());
        String genre = safe(genreField.getText());
        String release = safe(releaseField.getText());
        String link = safe(imdbField.getText());
        if (title.isEmpty()) { JOptionPane.showMessageDialog(this, "Title is required."); return; }
        try {
            String id = backend.BackendService.createMedia(title, genre, release, type, link);
            JOptionPane.showMessageDialog(this, "Added media with ID: " + id);
            titleField.setText(""); genreField.setText(""); releaseField.setText(""); imdbField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to add media.");
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
