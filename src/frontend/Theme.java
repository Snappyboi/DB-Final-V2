package frontend;

import java.awt.*;

public final class Theme {
    private Theme() {}

   
    public static final Color BACKGROUND       = new Color(18, 18, 18);
    public static final Color SURFACE          = new Color(24, 24, 24);
    public static final Color SURFACE_ELEVATED = new Color(28, 28, 28);
    public static final Color TEXT_PRIMARY     = new Color(245, 245, 245);
    public static final Color TEXT_SECONDARY   = new Color(200, 200, 200);

    public static final Color ACCENT_RED       = new Color(229, 9, 20);
    public static final Color ACCENT_GOLD      = new Color(212, 175, 55);

    public static final Color BUTTON_BG        = new Color(40, 40, 40);
    public static final Color BUTTON_BG_HOVER  = new Color(55, 55, 55);

    public static final int RADIUS = 12;

    
    public static final Color BB_BLUE   = new Color(0x00, 0x33, 0xA0);
    public static final Color BB_YELLOW = new Color(255, 220, 90); 

    public static Font fontRegular(int size) {
        return new Font("SansSerif", Font.PLAIN, size);
    }

    public static Font fontMedium(int size) {
        return new Font("SansSerif", Font.BOLD, size - 1);
    }

    public static Font fontBold(int size) {
        return new Font("SansSerif", Font.BOLD, size);
    }
}
