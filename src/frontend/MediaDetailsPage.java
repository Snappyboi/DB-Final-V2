package frontend;

import backend.BackendService;
import frontend.components.NavBar;
import frontend.components.RoundedButton;
import frontend.components.SurfacePanel;
import frontend.components.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

// Details screen for one title. Shows poster/info + IMDb link and Watch Now.
public class MediaDetailsPage extends JPanel {
    private final Navigation nav;
    private JLabel titleLabel;
    private JLabel descriptionLabel;
    private JLabel directorLabel;
    private JLabel castLabel;
    private JLabel genreLabel;
    private JLabel releaseLabel;
    private JLabel imdbLink;
    private JLabel posterLabel;
    private String currentImdbUrl; // populated from backend when media set

    public MediaDetailsPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(16, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.anchor = GridBagConstraints.NORTHWEST;

        // Poster
        SurfacePanel posterCard = new SurfacePanel();
        posterCard.setLayout(new BorderLayout());
        posterCard.setPreferredSize(new Dimension(260, 380));
        posterLabel = ImageUtils.createPosterLabelForTitle("", 260, 380);
        posterCard.add(posterLabel, BorderLayout.CENTER);

        gc.gridx = 0; gc.gridy = 0; gc.gridheight = 7; // span all rows on the left
        content.add(posterCard, gc);

        // Title
        titleLabel = new JLabel("Title");
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setFont(Theme.fontBold(28));
        gc.gridx = 1; gc.gridy = 0; gc.gridheight = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        content.add(titleLabel, gc);

        // Description
        descriptionLabel = new JLabel("Movie description."); // needs done still
        descriptionLabel.setForeground(Theme.TEXT_SECONDARY);
        descriptionLabel.setFont(Theme.fontRegular(14));
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        gc.gridy = 1;
        content.add(descriptionLabel, gc);

        directorLabel = labelRow("Director", "-");
        castLabel = labelRow("Cast", "-");
        genreLabel = labelRow("Genre", "-");
        releaseLabel = labelRow("Release", "-");

        gc.gridy = 2; content.add(directorLabel, gc);
        gc.gridy = 3; content.add(castLabel, gc);
        gc.gridy = 4; content.add(genreLabel, gc);
        gc.gridy = 5; content.add(releaseLabel, gc);

        // IMDb link and Watch button
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        imdbLink = linkLabel("View on IMDb");
        RoundedButton watch = new RoundedButton("Watch Now").red();
        actions.add(imdbLink);
        actions.add(watch);
        gc.gridy = 6; gc.fill = GridBagConstraints.NONE;
        content.add(actions, gc);

        // Watch Now:  write Watch_History
        watch.addActionListener(e -> {
            String username = nav.getCurrentUsername();
            String title = titleLabel.getText();
            JOptionPane.showMessageDialog(this, "Now streaming " + title, "ACED", JOptionPane.INFORMATION_MESSAGE);

            try {
                if (username == null || username.isEmpty()) {
                    throw new IllegalStateException("User not logged in");
                }
                // Look up media to get  DB id
                backend.Media media = BackendService.getMediaByTitle(title);
                if (media == null || media.getMediaIdRaw() == null || media.getMediaIdRaw().isEmpty()) {
                    throw new IllegalStateException("Unable to resolve media ID for '" + title + "'");
                }
                // Get member id from username
                int memberId = BackendService.getMemberIdByUsername(username);
                if (memberId <= 0) {
                    throw new IllegalStateException("Unable to resolve member ID for user '" + username + "'");
                }
                // Insert into Watch_History
                BackendService.addMediaToWatchHistory(memberId, media.getMediaIdRaw());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to add media to watch history", "ACED", JOptionPane.WARNING_MESSAGE);
            }

            // Video playback placeholder
            JOptionPane.showMessageDialog(this, "Play video placeholder.", "ACED", JOptionPane.INFORMATION_MESSAGE);
        });

        add(content, BorderLayout.CENTER);
    }

    private JLabel labelRow(String key, String value) {
        JLabel label = new JLabel(key + ": " + value);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setFont(Theme.fontRegular(14));
        return label;
    }

    private JLabel linkLabel(String text) {
        JLabel l = new JLabel("<html><u>" + text + "</u></html>");
        l.setForeground(Theme.ACCENT_GOLD);
        l.setFont(Theme.fontMedium(14));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openImdbCurrent(); }
            @Override public void mouseEntered(MouseEvent e) { l.setForeground(Theme.ACCENT_GOLD.darker()); }
            @Override public void mouseExited(MouseEvent e) { l.setForeground(Theme.ACCENT_GOLD); }
        });
        return l;
    }

    private void openImdbCurrent() {
        String link = currentImdbUrl;
        if (link == null || link.isBlank()) {
            JOptionPane.showMessageDialog(this, "No IMDb link.");
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(link));
            } else {
                JOptionPane.showMessageDialog(this, "Cannot open browser .");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to open link:\n" + e.getMessage());
        }
    }

    public void setMedia(String title) {
        titleLabel.setText(title);
        // Pull details from backend
        String dir = "-", cast = "-", genre = "-", release = "-";
        currentImdbUrl = null;
        try {
            backend.Media m = BackendService.getMediaDetailsByTitle(title);
            if (m == null) m = BackendService.getMediaByTitle(title);
            if (m != null) {
                if (m.getImdbLink() != null && !m.getImdbLink().isBlank()) currentImdbUrl = m.getImdbLink();
                if (m.getDirectors() != null && !m.getDirectors().isBlank()) dir = cleanList(m.getDirectors());
                if (m.getCast() != null && !m.getCast().isBlank()) cast = cleanList(m.getCast());
                if (m.getGenre() != null && !m.getGenre().isBlank()) genre = cleanList(m.getGenre());
                if (m.getReleaseDate() != null && !m.getReleaseDate().isBlank()) release = m.getReleaseDate();
            }
        } catch (Exception ignored) {}


        descriptionLabel.setText("Info: " + titleLabel.getText());

        setField(directorLabel, "Director", dir);
        setField(castLabel, "Cast", cast);
        setField(genreLabel, "Genre", genre);
        setField(releaseLabel, "Release", release);

        // Load poster
        JLabel newPoster = ImageUtils.createPosterLabelForTitle(title, 260, 380);
        posterLabel.setIcon(newPoster.getIcon());
        posterLabel.setText(newPoster.getText());
        posterLabel.setBorder(newPoster.getBorder());
        posterLabel.repaint();
    }

    // Keep strings short
    private void setField(JLabel label, String key, String value) {
        String show = value == null || value.isBlank() ? "-" : value.trim();
        String full = show;
        if (show.length() > 120) {
            show = show.substring(0, 117) + "...";
            label.setToolTipText(full);
        } else {
            label.setToolTipText(null);
        }
        label.setText(key + ": " + show);
    }

    private String cleanList(String s) {
        if (s == null) return "-";
        String out = s.replaceAll("\\s+,", ",").replaceAll(",\\s+", ", ").replaceAll("\\s+", " ").trim();
        if (out.endsWith(",")) out = out.substring(0, out.length() - 1).trim();
        return out.isEmpty() ? "-" : out;
    }
}
