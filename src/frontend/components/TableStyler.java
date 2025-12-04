package frontend.components;

import frontend.admin.AdminTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

// Helper to style admin tables
public final class TableStyler {
    private TableStyler() {}

    public static void applyAdminStyle(JTable table) {
        if (table == null) return;
        table.setBackground(AdminTheme.SURFACE);
        table.setForeground(AdminTheme.TEXT_PRIMARY);
        table.setGridColor(AdminTheme.GRID);
        table.setRowHeight(34);
        table.setFont(AdminTheme.fontRegular(13));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setBackground(AdminTheme.SURFACE);
            header.setForeground(AdminTheme.TEXT_SECONDARY);
            header.setFont(AdminTheme.fontMedium(13));
        }

        // striping for readability
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground((row % 2 == 0) ? AdminTheme.SURFACE : AdminTheme.SURFACE_ELEVATED);
                    c.setForeground(AdminTheme.TEXT_PRIMARY);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                }
                return c;
            }
        });
    }
}
