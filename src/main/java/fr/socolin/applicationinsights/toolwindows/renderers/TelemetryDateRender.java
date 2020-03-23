package fr.socolin.applicationinsights.toolwindows.renderers;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TelemetryDateRender extends TelemetryRenderBase {

    private SimpleDateFormat simpleDateFormat;

    public TelemetryDateRender() {
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss.S");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Date date = (Date) value;

        super.setText(simpleDateFormat.format(date));

        return this;
    }
}
