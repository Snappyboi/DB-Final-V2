package frontend.admin;

import java.awt.*;

// Dark theme for admin pages
public final class AdminTheme {
    private AdminTheme() {}

    public static final Color BACKGROUND = frontend.Theme.BACKGROUND;
    public static final Color SURFACE = frontend.Theme.SURFACE;
    public static final Color SURFACE_ELEVATED = frontend.Theme.SURFACE_ELEVATED;
    public static final Color TEXT_PRIMARY = frontend.Theme.TEXT_PRIMARY;
    public static final Color TEXT_SECONDARY = frontend.Theme.TEXT_SECONDARY;
    public static final Color GRID = new Color(70, 70, 70);

    public static final Color ACCENT_GOLD = frontend.Theme.ACCENT_GOLD;
    public static final int RADIUS = frontend.Theme.RADIUS;

    public static Font fontRegular(int size) { return frontend.Theme.fontRegular(size); }
    public static Font fontMedium(int size) { return frontend.Theme.fontMedium(size); }
    public static Font fontBold(int size) { return frontend.Theme.fontBold(size); }
}
