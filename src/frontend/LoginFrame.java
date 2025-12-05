package frontend;

import backend.BackendService;
import frontend.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class LoginFrame extends JPanel {
    private final Navigation nav;
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final RoundedButton loginBtn = new RoundedButton("Login");
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame(Navigation nav) {
        this.nav = nav;

        // deep blue backdrop
        setLayout(new BorderLayout());
        setBackground(new Color(12, 18, 46));

        // centered ACED logo 
        JLabel logo = new JLabel();
        logo.setOpaque(false);
        logo.setIcon(loadAcedLogoByWidth(240)); // slightly larger

        JLabel tagline = new JLabel("Welcome to your home theatre");
        tagline.setFont(Theme.fontRegular(14));
        tagline.setForeground(new Color(210, 210, 230));

        JPanel titleBar = new JPanel();
        titleBar.setOpaque(false);
        titleBar.setLayout(new BoxLayout(titleBar, BoxLayout.Y_AXIS));
        titleBar.setBorder(new EmptyBorder(30, 0, 10, 0));

        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleBar.add(Box.createVerticalStrut(20));
        titleBar.add(logo);
        titleBar.add(Box.createVerticalStrut(8));
        titleBar.add(tagline);

        add(titleBar, BorderLayout.NORTH);

        // yellow ticket 
        TicketPanel formCard = new TicketPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        gc.weightx = 0.0;
        gc.insets = new Insets(4, 2, 2, 2);

        JLabel uLabel = new JLabel("Username");
        uLabel.setForeground(new Color(10, 40, 100));
        uLabel.setFont(Theme.fontRegular(15));
        form.add(uLabel, gc);
        gc.gridy++;
        gc.insets = new Insets(2, 2, 6, 2);

        stylizeField(username);
        username.setColumns(12);
        Dimension userSize = username.getPreferredSize();
        username.setPreferredSize(userSize);
        username.setMaximumSize(userSize);
        form.add(username, gc);
        gc.gridy++;
        gc.insets = new Insets(4, 2, 2, 2);

        JLabel pLabel = new JLabel("Password");
        pLabel.setForeground(new Color(10, 40, 100));
        pLabel.setFont(Theme.fontRegular(15));
        form.add(pLabel, gc);
        gc.gridy++;
        gc.insets = new Insets(2, 2, 8, 2);

        stylizeField(password);
        password.setColumns(12);
        Dimension passSize = password.getPreferredSize();
        password.setPreferredSize(passSize);
        password.setMaximumSize(passSize);
        form.add(password, gc);
        gc.gridy++;

        // Buttons row
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        // Modern, flat-ish buttons
        loginBtn.setBackground(Theme.BB_BLUE);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setHorizontalAlignment(SwingConstants.CENTER);
        loginBtn.setMargin(new Insets(8, 22, 8, 22));

        RoundedButton registerBtn = new RoundedButton("Register");
        registerBtn.setBackground(Theme.BB_YELLOW);
        registerBtn.setForeground(Theme.BB_BLUE);
        registerBtn.setHorizontalAlignment(SwingConstants.CENTER);
        registerBtn.setMargin(new Insets(8, 22, 8, 22));

        buttons.add(loginBtn);
        buttons.add(registerBtn);

        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        form.add(buttons, gc);
        gc.gridy++;

        statusLabel.setForeground(new Color(200, 40, 40));
        statusLabel.setFont(Theme.fontRegular(12));
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(statusLabel, gc);

        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(Box.createVerticalGlue());
        formCard.add(form);
        formCard.add(Box.createVerticalGlue());

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        // keep some breathing room around the ticket
        centerWrap.setBorder(new EmptyBorder(24, 0, 32, 0));
        centerWrap.add(formCard);

        add(centerWrap, BorderLayout.CENTER);

        // ===== Actions =====
        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Registration placeholder.",
                "ACED",
                JOptionPane.INFORMATION_MESSAGE
        ));
    }

    // Expose login button for app shell
    public JButton getDefaultButton() { return loginBtn; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(8, 14, 32), 0, h, new Color(18, 28, 60));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }

    // ===== Login logic (same as your original) =====
    private void doLogin() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Enter both username and password.");
            return;
        }

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

    // ===== Logo loading / scaling =====
    private ImageIcon loadAcedLogoByWidth(int targetW) {
        try {
            java.net.URL url = getClass().getResource("/resources/ACEDLogo.png");
            Image img = null;
            if (url != null) {
                img = new ImageIcon(url).getImage();
            } else {
                java.io.File f = new java.io.File("src/resources/ACEDLogo.png");
                if (f.exists()) img = new ImageIcon(f.getAbsolutePath()).getImage();
            }

            if (img != null) {
                int w = img.getWidth(null);
                int h = img.getHeight(null);
                if (w > 0 && h > 0) {
                    int newW = Math.max(16, targetW);
                    int newH = (int) Math.round((double) h * newW / (double) w);
                    Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return new ImageIcon(img);
            }
        } catch (Exception ignored) { }

        return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    }

    // ===== Field styling + modern focus ring =====
    private void stylizeField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(15, 35, 90));
        field.setCaretColor(new Color(15, 35, 90));
        field.setFont(Theme.fontRegular(12));

        // base border
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BB_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        // focus ring
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BB_BLUE, 2),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BB_BLUE, 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }
        });
    }

    // ===== Inner class that draws the yellow ticket + soft spotlight =====
    private class TicketPanel extends JPanel {
        TicketPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(420, 260));
            setBorder(new EmptyBorder(24, 32, 24, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // ---- Ticket shape ----
            int margin = 8;
            int rx = margin;
            int ry = margin;
            int rw = w - margin * 2;
            int rh = h - margin * 2;
            int arc = 40; // slightly rounder corners

            RoundRectangle2D outer = new RoundRectangle2D.Float(rx, ry, rw, rh, arc, arc);
            Area ticket = new Area(outer);

            int notchRadius = 26;
            Ellipse2D leftNotch = new Ellipse2D.Float(
                    rx - notchRadius / 2f,
                    ry + rh / 2f - notchRadius / 2f,
                    notchRadius,
                    notchRadius
            );
            Ellipse2D rightNotch = new Ellipse2D.Float(
                    rx + rw - notchRadius / 2f,
                    ry + rh / 2f - notchRadius / 2f,
                    notchRadius,
                    notchRadius
            );
            ticket.subtract(new Area(leftNotch));
            ticket.subtract(new Area(rightNotch));

            // Soft spotlight clipped to ticket to avoid outside glow
            int glowSize = Math.max(w, h);
            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point2D.Float(w / 2f, h / 2f),
                    glowSize * 0.6f,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(255, 255, 255, 140),
                            new Color(12, 18, 46, 0)
                    }
            );
            Shape oldClip = g2.getClip();
            g2.setClip(ticket);
            g2.setPaint(glow);
            g2.fill(ticket.getBounds2D());
            g2.setClip(oldClip);

            // Fill ticket with slight gradient
            Color top = Theme.BB_YELLOW;
            Color bottom = new Color(0xFF, 0xCC, 0x00);
            GradientPaint fill = new GradientPaint(0, ry, top, 0, ry + rh, bottom);
            g2.setPaint(fill);
            g2.fill(ticket);

            // Outer border (slightly thinner) using Blockbuster blue
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(Theme.BB_BLUE);
            g2.draw(ticket);

            // Inner decorative border
            int inset = 12;
            RoundRectangle2D inner = new RoundRectangle2D.Float(
                    rx + inset, ry + inset,
                    rw - inset * 2, rh - inset * 2,
                    arc - 10, arc - 10
            );
            Area innerTicket = new Area(inner);

            Ellipse2D leftInner = new Ellipse2D.Float(
                    rx + inset - notchRadius / 2f,
                    ry + rh / 2f - notchRadius / 2f,
                    notchRadius,
                    notchRadius
            );
            Ellipse2D rightInner = new Ellipse2D.Float(
                    rx + rw - inset - notchRadius / 2f,
                    ry + rh / 2f - notchRadius / 2f,
                    notchRadius,
                    notchRadius
            );
            innerTicket.subtract(new Area(leftInner));
            innerTicket.subtract(new Area(rightInner));

            g2.draw(innerTicket);

            // Perforation line near the right notch
            float dashX = rx + rw - notchRadius - 26;
            Stroke dashed = new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{7f, 6f}, 0f);
            g2.setStroke(dashed);
            g2.setColor(new Color(0, 0, 0, 90));
            g2.drawLine((int) dashX, ry + 12, (int) dashX, ry + rh - 12);

            g2.dispose();
        }
    }
}
