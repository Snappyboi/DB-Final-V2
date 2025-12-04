package frontend.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

// Loads poster images and builds labels
public final class ImageUtils {
    private ImageUtils() {}

    public static JLabel createPosterLabel(String resourcePath) {
        JLabel label = new JLabel();
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(160, 240));
        label.setMinimumSize(new Dimension(160, 240));
        label.setMaximumSize(new Dimension(160, 240));

        try {
            Image img = loadPosterImage(resourcePath, 160, 240);
            if (img != null) {
                label.setIcon(new ImageIcon(img));
            } else {
                label.setText("No Image");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setForeground(new Color(200,200,200));
                label.setBorder(BorderFactory.createLineBorder(new Color(70,70,70)));
            }
        } catch (Exception e) {
            label.setText("Error");
        }

        return label;
    }

    public static Image loadPosterImage(String resourcePath, int w, int h) {
        try {
            URL url = ImageUtils.class.getResource(resourcePath);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                return icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            }

            String normalized = resourcePath;
            if (normalized.startsWith("/")) normalized = normalized.substring(1);
            File devFile = new File("src", normalized);
            if (!devFile.exists()) {
                if (normalized.startsWith("MoviePosters/")) {
                    devFile = new File(normalized);
                }
            }
            if (!devFile.exists()) {
                if (normalized.startsWith("MoviePosters/")) {
                    devFile = new File("src/resources", normalized.substring("MoviePosters/".length()));
                    if (!devFile.exists()) {
                        devFile = new File("resources", normalized.substring("MoviePosters/".length()));
                    }
                }
            }
            if (devFile.exists() && devFile.isFile()) {
                ImageIcon icon = new ImageIcon(devFile.getAbsolutePath());
                return icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Image loadPosterImageForTitle(String title, int w, int h) {
        if (title == null || title.isEmpty()) return null;
        String base = title.trim();
        List<String> names = buildFilenameCandidates(base);
        List<String> exts = Arrays.asList(".jpg", ".png", ".jpeg", ".webp");
        for (String name : names) {
            for (String ext : exts) {
                String path = "/MoviePosters/" + name + ext;
                Image img = loadPosterImage(path, w, h);
                if (img != null) return img;
            }
        }
        String wantedKey = normalizeForMatch(base);
        String bestFile = (wantedKey == null || wantedKey.isBlank()) ? null : findBestPosterMatch(wantedKey);
        if (bestFile != null) {
            String path = "/MoviePosters/" + bestFile;
            Image img = loadPosterImage(path, w, h);
            if (img != null) return img;
        }
        return createPlaceholderPoster(base, w, h);
    }

    public static JLabel createPosterLabelForTitle(String title) {
        return createPosterLabelForTitle(title, 160, 240);
    }

    public static JLabel createPosterLabelForTitle(String title, int w, int h) {
        JLabel label = new JLabel();
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(w, h));
        label.setMinimumSize(new Dimension(w, h));
        label.setMaximumSize(new Dimension(w, h));
        Image img = loadPosterImageForTitle(title, w, h);
        if (img != null) {
            label.setIcon(new ImageIcon(img));
        } else {
            label.setText("No Image");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(new Color(200,200,200));
            label.setBorder(BorderFactory.createLineBorder(new Color(70,70,70)));
        }
        return label;
    }

    public static List<String> listPosterFiles() {
        List<String> files = new ArrayList<>();
        try {
            URL dirUrl = ImageUtils.class.getResource("/MoviePosters");
            if (dirUrl != null && "file".equalsIgnoreCase(dirUrl.getProtocol())) {
                File dir = new File(dirUrl.toURI());
                if (dir.isDirectory()) collectImageFiles(dir, files);
            } else {
                File devDir = new File("src/MoviePosters");
                if (devDir.isDirectory()) collectImageFiles(devDir, files);
                File devResDir = new File("src/resources/MoviePosters");
                if (devResDir.isDirectory()) collectImageFiles(devResDir, files);
                File resDir = new File("resources/MoviePosters");
                if (resDir.isDirectory()) collectImageFiles(resDir, files);
            }
        } catch (Exception ignored) {}
        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    private static void collectImageFiles(File dir, List<String> out) {
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isFile()) {
                String name = f.getName();
                String lower = name.toLowerCase();
                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                    if (!out.contains(name)) out.add(name);
                }
            }
        }
    }

    private static String capitalizeWords(String s) {
        String[] parts = s.trim().split("\\s+");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            String word = parts[i];
            String cap = word.substring(0, 1).toUpperCase() + (word.length() > 1 ? word.substring(1) : "");
            if (i > 0) b.append(' ');
            b.append(cap);
        }
        return b.toString();
    }

    private static List<String> buildFilenameCandidates(String title) {
        List<String> candidates = new ArrayList<>();
        String t = stripMeta(title.trim());
        String cleaned = t
                .replace("&", "and")
                .replace(":", " ")
                .replace("-", " ")
                .replace("—", " ")
                .replace("–", " ")
                .replace("_", " ")
                .replace("/", " ")
                .replace("\\", " ");
        cleaned = cleaned.replaceAll("[\'\"]", "");
        cleaned = cleaned.replaceAll("[^A-Za-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        String stripped = cleaned.replaceFirst("^(?i)(the |a |an )", "").trim();

        String spacedCap = capitalizeWords(cleaned);
        String spacedLower = spacedCap.toLowerCase();
        String compactCap = spacedCap.replace(" ", "");
        String compactLower = spacedLower.replace(" ", "");

        String strippedCap = capitalizeWords(stripped);
        String strippedLower = strippedCap.toLowerCase();
        String strippedCompactCap = strippedCap.replace(" ", "");
        String strippedCompactLower = strippedLower.replace(" ", "");

        String originalCap = capitalizeWords(t.replaceAll("\\s+", " ").trim());
        String originalCompact = originalCap.replace(" ", "");

        addUnique(candidates, originalCompact);
        addUnique(candidates, originalCap);
        addUnique(candidates, spacedCap);
        addUnique(candidates, compactCap);
        addUnique(candidates, spacedLower);
        addUnique(candidates, compactLower);
        addUnique(candidates, strippedCompactCap);
        addUnique(candidates, strippedCap);
        addUnique(candidates, strippedCompactLower);
        addUnique(candidates, strippedLower);
        return candidates;
    }

    private static void addUnique(List<String> list, String value) {
        if (value == null || value.isEmpty()) return;
        if (!list.contains(value)) list.add(value);
    }

    private static volatile Map<String, String> posterKeyToFile;
    private static volatile long posterCacheStamp = 0L;

    private static String normalizeForMatch(String s) {
        if (s == null) return "";
        String t = stripMeta(s);
        t = t.toLowerCase();
        t = t.replace('&', ' ');
        t = t.replace('-', ' ');
        t = t.replace('—', ' ');
        t = t.replace('–', ' ');
        t = t.replace('_', ' ');
        t = t.replace('/', ' ');
        t = t.replace('\\', ' ');
        t = t.replace(':', ' ');
        t = t.replace('.', ' ');
        t = t.replace(',', ' ');
        t = t.replace('\'', ' ');
        t = t.replace('"', ' ');
        t = t.replaceAll("[^a-z0-9 ]", " ");
        t = t.replaceFirst("^(the |a |an )", "");
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    private static String stripMeta(String t) {
        if (t == null) return "";
        String s = t;
        String trimmed = s.trim();
        if (trimmed.matches("^(19|20)\\d{2}$")) { return trimmed; }
        s = s.replaceAll("\\([^)]*\\)", " ");
        s = s.replaceAll("\\[[^]]*\\]", " ");
        s = s.replaceAll("(?i)\\bseason\\s*\\d+\\b", " ");
        s = s.replaceAll("(?i)\\bs\\d{1,2}\\b", " ");
        s = s.replaceAll("\\b(19|20)\\d{2}\\b", " ");
        s = s.trim();
        if (s.isEmpty()) return trimmed;
        return s;
    }

    private static synchronized void ensurePosterKeyCache() {
        long now = System.currentTimeMillis();
        if (posterKeyToFile != null && (now - posterCacheStamp) < 5000) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        List<String> files = listPosterFiles();
        for (String f : files) {
            String base = f;
            int dot = base.lastIndexOf('.');
            if (dot > 0) base = base.substring(0, dot);
            String key = normalizeForMatch(base);
            if (!key.isEmpty()) {
                map.putIfAbsent(key, f);
            }
        }
        posterKeyToFile = map;
        posterCacheStamp = now;
    }

    private static String findBestPosterMatch(String wantedKey) {
        ensurePosterKeyCache();
        if (posterKeyToFile == null || posterKeyToFile.isEmpty()) return null;
        if (wantedKey == null || wantedKey.isBlank()) return null;

        String bestFile = null;
        int bestScore = -1;

        String exact = posterKeyToFile.get(wantedKey);
        if (exact != null) return exact;

        Set<String> wantedTokens = new HashSet<>(Arrays.asList(wantedKey.split(" ")));
        wantedTokens.remove("");

        for (Map.Entry<String, String> e : posterKeyToFile.entrySet()) {
            String key = e.getKey();
            String file = e.getValue();

            int score = 0;
            if (key.equals(wantedKey)) {
                score = 100;
            } else if (key.startsWith(wantedKey) || wantedKey.startsWith(key)) {
                score = 95;
            } else if (key.contains(wantedKey) || wantedKey.contains(key)) {
                score = 90;
            } else {
                Set<String> candTokens = new HashSet<>(Arrays.asList(key.split(" ")));
                candTokens.remove("");
                if (!wantedTokens.isEmpty() && !candTokens.isEmpty()) {
                    int inter = 0;
                    for (String t : wantedTokens) { if (candTokens.contains(t)) inter++; }
                    int union = wantedTokens.size() + candTokens.size() - inter;
                    if (union > 0) {
                        double j = (double) inter / (double) union;
                        score = (int) Math.round(j * 85);
                    }
                }
                if (score < 75) {
                    String wtFirst = firstToken(wantedKey);
                    String wtLast = lastToken(wantedKey);
                    String ckFirst = firstToken(key);
                    String ckLast = lastToken(key);
                    if (!wtFirst.isEmpty() && wtFirst.equals(ckFirst)) score = Math.max(score, 60);
                    if (!wtLast.isEmpty() && wtLast.equals(ckLast)) score = Math.max(score, 60);
                }
            }

            if (score > bestScore) { bestScore = score; bestFile = file; }
        }
        return (bestScore >= 50) ? bestFile : null;
    }

    private static String firstToken(String s) {
        if (s == null || s.isEmpty()) return "";
        int i = s.indexOf(' ');
        return (i < 0) ? s : s.substring(0, i);
    }

    private static String lastToken(String s) {
        if (s == null || s.isEmpty()) return "";
        int i = s.lastIndexOf(' ');
        return (i < 0) ? s : s.substring(i + 1);
    }

    private static Image createPlaceholderPoster(String title, int w, int h) {
        int width = Math.max(40, w);
        int height = Math.max(60, h);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = new Color(28, 28, 30);
            Color border = new Color(70, 70, 70);
            g.setColor(bg);
            g.fillRect(0, 0, width, height);
            g.setColor(border);
            g.drawRect(0, 0, width - 1, height - 1);
            String initials = computeInitials(title, 2);
            int circleW = (int)(width * 0.65);
            int circleH = (int)(height * 0.65);
            int cx = (width - circleW) / 2;
            int cy = (height - circleH) / 2;
            g.setColor(new Color(255, 215, 0, 26));
            g.fillOval(cx, cy, circleW, circleH);
            g.setColor(new Color(235, 235, 235));
            int fontSize = Math.max(18, Math.min(width, height) / 3);
            g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            FontMetrics fm = g.getFontMetrics();
            int tx = (width - fm.stringWidth(initials)) / 2;
            int ty = (height + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(initials, tx, ty);
        } finally {
            g.dispose();
        }
        return img;
    }

    private static String computeInitials(String title, int maxChars) {
        if (title == null || title.trim().isEmpty()) return "?";
        String[] parts = title.trim().split("\\s+");
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            char c = p.charAt(0);
            if (Character.isLetterOrDigit(c)) b.append(Character.toUpperCase(c));
            if (b.length() >= maxChars) break;
        }
        if (b.length() == 0) return "?";
        return b.toString();
    }
}
