// Login screen (DB_Final name: LoginFrame)
package frontend;

import backend.BackendService;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JPanel {
    private final Navigation nav;
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final RoundedButton loginBtn = new RoundedButton("Login").gold();
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        // Title (logo + Streaming)
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        titleBar.setOpaque(false);
        titleBar.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JLabel streamingWord = new JLabel("Streaming");
        streamingWord.setForeground(Theme.TEXT_PRIMARY);
        streamingWord.setFont(Theme.fontBold(56));

        JLabel logo = new JLabel();
        logo.setOpaque(false);
        logo.setIcon(loadAcedLogoByWidth(200));
        titleBar.add(logo);
        streamingWord.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        titleBar.add(streamingWord);
        add(titleBar, BorderLayout.NORTH);

        // Form card
        SurfacePanel formCard = new SurfacePanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(420, 340));
        formCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        gc.insets = new Insets(4,4,4,4);

        JLabel uLabel = new JLabel("Username");
        uLabel.setForeground(Theme.TEXT_SECONDARY);
        uLabel.setFont(Theme.fontRegular(14));
        form.add(uLabel, gc);
        gc.gridy++;

        stylizeField(username);
        form.add(username, gc);
        gc.gridy++;

        JLabel pLabel = new JLabel("Password");
        pLabel.setForeground(Theme.TEXT_SECONDARY);
        pLabel.setFont(Theme.fontRegular(14));
        form.add(pLabel, gc);
        gc.gridy++;

        stylizeField(password);
        form.add(password, gc);
        gc.gridy++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.add(loginBtn);
        RoundedButton registerBtn = new RoundedButton("Register");
        buttons.add(registerBtn);
        gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.CENTER;
        form.add(buttons, gc);
        gc.gridy++;

        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(Theme.fontRegular(12));
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(statusLabel, gc);

        formCard.add(form);
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(formCard);
        add(centerWrap, BorderLayout.CENTER);

        // Actions
        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Registration placeholder.", "ACED", JOptionPane.INFORMATION_MESSAGE));
    }

    // Expose  button for app shell
    public JButton getDefaultButton() { return loginBtn; }

    private void doLogin() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) { statusLabel.setText("Enter both username and password."); return; }

        if (BackendService.loginAdmin(user, pass)) {
            nav.setCurrentUsername(user);
            if (nav instanceof AdminAware) { ((AdminAware) nav).setAdmin(true); }
            nav.showAdminHome();
        } else if (BackendService.loginMember(user, pass)) {
            nav.setCurrentUsername(user);
            if (nav instanceof AdminAware) { ((AdminAware) nav).setAdmin(false); }
            nav.showMemberHome();
        } else {
            statusLabel.setText("Incorrect username or password.");
        }
    }

    private ImageIcon loadAcedLogoByWidth(int targetW) {
        try {
            java.net.URL url = getClass().getResource("/resources/ACEDLogo.png");
            java.awt.Image img = null;
            if (url != null) img = new ImageIcon(url).getImage();
            else {
                java.io.File f = new java.io.File("src/resources/ACEDLogo.png");
                if (f.exists()) img = new ImageIcon(f.getAbsolutePath()).getImage();
            }
            if (img != null) {
                int w = img.getWidth(null), h = img.getHeight(null);
                if (w > 0 && h > 0) {
                    int newW = Math.max(16, targetW);
                    int newH = (int) Math.round((double) h * newW / (double) w);
                    java.awt.Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return new ImageIcon(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    }

    private void stylizeField(JTextField field) {
        field.setBackground(Theme.SURFACE);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setFont(Theme.fontRegular(14));
    }
}
