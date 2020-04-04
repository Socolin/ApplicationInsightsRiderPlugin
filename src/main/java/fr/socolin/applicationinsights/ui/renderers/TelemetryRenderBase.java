package fr.socolin.applicationinsights.ui.renderers;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TelemetryRenderBase extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.setOpaque(true);
        if (isSelected) {
            super.setBackground(JBColor.namedColor("Table.selectionBackground", JBColor.blue));
        } else {
            super.setBackground(JBColor.namedColor("Table.background", JBColor.gray));
        }

        return this;
    }
}
