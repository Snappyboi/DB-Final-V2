package frontend;

import backend.BackendService;
import frontend.components.ImageUtils;
import frontend.components.NavBar;
import frontend.components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Watch History: shows what the user watched.
public class WatchHistoryPage extends JPanel {
    private final Navigation nav;
    private JPanel resultsPanel;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int CARD_POSTER_WIDTH = 140;
    private static final int CARD_POSTER_HEIGHT = 210;

    public WatchHistoryPage(Navigation nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new NavBar(nav, true), BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JLabel title = new JLabel("My Watch History");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.fontBold(20));
        title.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));
        content.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    // Load history and show cards
    public void refresh() {
        resultsPanel.removeAll();
        String username = nav.getCurrentUsername();
        Map<String, HistoryItem> historyByTitle = loadHistoryWithTimes(username);

        if (historyByTitle.isEmpty()) {
            JLabel empty = new JLabel("No watch history yet.");
            empty.setForeground(Theme.TEXT_SECONDARY);
            empty.setFont(Theme.fontRegular(14));
            empty.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
            resultsPanel.setLayout(new BorderLayout());
            resultsPanel.add(empty, BorderLayout.NORTH);
        } else {
            resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
            for (Map.Entry<String, HistoryItem> entry : historyByTitle.entrySet()) {
                resultsPanel.add(buildHistoryCard(entry.getKey(), entry.getValue()));
                resultsPanel.add(Box.createVerticalStrut(12));
            }
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private Map<String, HistoryItem> loadHistoryWithTimes(String username) {
        Map<String, HistoryItem> byTitle = new LinkedHashMap<>();
        List<String[]> rows;
        try {
            rows = BackendService.getWatchHistoryRowsByUsername(username);
        } catch (Exception e) {
            rows = new ArrayList<>();
        }
        if (rows == null || rows.isEmpty()) return byTitle;

        for (String[] r : rows) {
            if (r == null || r.length == 0) continue;
            String title = r[0];
            if (title == null || title.isBlank()) continue;
            String when = (r.length > 1) ? formatWatchDate(r[1]) : "-";
            String imdb = (r.length > 2) ? r[2] : null;
            HistoryItem item = byTitle.computeIfAbsent(title, k -> new HistoryItem());
            item.times.add(when);
            if (imdb != null && (item.imdbLink == null || item.imdbLink.isBlank())) item.imdbLink = imdb;
        }
        return byTitle;
    }

    private String formatWatchDate(String raw) {
        if (raw == null || raw.isBlank()) return "-";
        try {
            LocalDateTime dt = Timestamp.valueOf(raw).toLocalDateTime();
            return dt.format(TIME_FORMATTER);
        } catch (IllegalArgumentException ignored) {
            // Fall through to try parsing as ISO/local date-time
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(raw);
            return dt.format(TIME_FORMATTER);
        } catch (DateTimeParseException ignored) { }
        return raw;
    }

    private JPanel buildHistoryCard(String title, HistoryItem item) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setOpaque(true);
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        Image img = ImageUtils.loadPosterImageForTitle(title, CARD_POSTER_WIDTH, CARD_POSTER_HEIGHT);
        JLabel poster = new JLabel(new ImageIcon(img));
        poster.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 120), 1));
        card.add(poster, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BorderLayout(0, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setFont(Theme.fontBold(18));
        right.add(titleLabel, BorderLayout.NORTH);

        JLabel timesLabel = new JLabel(buildTimesHtml(item.times));
        timesLabel.setForeground(Theme.TEXT_SECONDARY);
        timesLabel.setFont(Theme.fontRegular(14));
        right.add(timesLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        RoundedButton streamBtn = new RoundedButton("Stream again").red();
        streamBtn.addActionListener(e -> nav.showMediaDetails(title));
        actions.add(streamBtn);

        RoundedButton imdbBtn = new RoundedButton("View on IMDb").gold();
        imdbBtn.setEnabled(item.imdbLink != null && !item.imdbLink.isBlank());
        imdbBtn.addActionListener(e -> openImdb(item.imdbLink));
        actions.add(imdbBtn);

        right.add(actions, BorderLayout.SOUTH);
        card.add(right, BorderLayout.CENTER);
        return card;
    }

    private String buildTimesHtml(List<String> times) {
        if (times == null || times.isEmpty()) return "<html><span style='color:#c8c8c8;'>No watch times recorded.</span></html>";
        String list = times.stream()
                .map(t -> "• " + (t == null ? "-" : t))
                .collect(Collectors.joining("<br>"));
        return "<html><span style='color:#c8c8c8;'>Viewed at (24-hour):<br>" + list + "</span></html>";
    }

    private void openImdb(String link) {
        if (link == null || link.isBlank()) return;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(link));
            } else {
                JOptionPane.showMessageDialog(this, "IMDb link: " + link);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not open IMDb link. URL: " + link);
        }
    }

    private static class HistoryItem {
        List<String> times = new ArrayList<>();
        String imdbLink;
    }
}
