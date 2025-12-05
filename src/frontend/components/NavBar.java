package frontend.components;

import backend.BackendService;
import frontend.Navigation;
import frontend.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

// nav bar
public class NavBar extends JPanel {
    private static final int NAV_HEIGHT = 60;
    public NavBar(Navigation nav, boolean showAdmin) {
        setLayout(new BorderLayout());
        setBackground(new Color(12, 18, 46));
        int vPad = 0;
        setBorder(BorderFactory.createEmptyBorder(vPad, 24, vPad, 24));

        JLabel streaming = new JLabel("Streaming");
        streaming.setForeground(Theme.ACCENT_GOLD);
        streaming.setFont(Theme.fontBold(24));

        int targetH = Math.max(16, NAV_HEIGHT - (vPad * 2));

        JLabel logo = new JLabel();
        logo.setOpaque(false);
        logo.setIcon(loadAcedLogo(targetH));
        logo.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);
        left.add(logo);
        streaming.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        left.add(streaming);

        MouseAdapter homeClick = new MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { nav.showMemberHome(); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { setCursor(Cursor.getDefaultCursor()); }
        };
        logo.addMouseListener(homeClick);
        streaming.addMouseListener(homeClick);

        JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightFlow.setOpaque(false);
        rightFlow.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        RoundedButton browse = new RoundedButton("Browse").gold();
        RoundedButton history = new RoundedButton("My Watch History").gold();
        RoundedButton account = new RoundedButton("Account Info").gold();
        RoundedButton logout = new RoundedButton("Logout").red();

        browse.addActionListener(e -> nav.showBrowseSearch());
        history.addActionListener(e -> nav.showWatchHistory());
        account.addActionListener(e -> nav.showAccount());
        logout.addActionListener(e -> nav.logout(BackendService.getMemberIdByUsername(nav.getCurrentUsername())));

        rightFlow.add(browse);
        rightFlow.add(history);
        rightFlow.add(account);
        rightFlow.add(logout);

        RoundedButton adminBtn = new RoundedButton("Admin").gold();
        adminBtn.addActionListener(e -> nav.showAdminHome());
        boolean allowAdmin = false;
        if (nav instanceof frontend.AdminAware) allowAdmin = ((frontend.AdminAware) nav).isAdmin();
        adminBtn.setVisible(allowAdmin);
        rightFlow.add(adminBtn);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        right.add(rightFlow, gbc);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        final boolean supportsAdmin = nav instanceof frontend.AdminAware;
        if (supportsAdmin) {
            this.addAncestorListener(new javax.swing.event.AncestorListener() {
                private void update() {
                    boolean isAdmin = ((frontend.AdminAware) nav).isAdmin();
                    adminBtn.setVisible(isAdmin);
                    revalidate(); repaint();
                }
                @Override public void ancestorAdded(javax.swing.event.AncestorEvent event) { update(); }
                @Override public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
                @Override public void ancestorMoved(javax.swing.event.AncestorEvent event) { update(); }
            });
        }

        Dimension pref = new Dimension(Integer.MAX_VALUE, NAV_HEIGHT);
        setPreferredSize(pref);
        setMinimumSize(new Dimension(0, NAV_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_HEIGHT));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, NAV_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        return new Dimension(d.width, NAV_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return new Dimension(d.width, NAV_HEIGHT);
    }

    // Load logo (small helper)
    private ImageIcon loadAcedLogo(int targetH) {
        try {
            java.net.URL url = getClass().getResource("/resources/ACEDLogo.png");
            java.awt.Image img = null;
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
                    int newH = Math.max(16, targetH);
                    int newW = (int) Math.round((double) w * newH / (double) h);
                    java.awt.Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return new ImageIcon(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    }
}
