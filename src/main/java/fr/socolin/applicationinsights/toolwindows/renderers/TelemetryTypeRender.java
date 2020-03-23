package fr.socolin.applicationinsights.toolwindows.renderers;

import com.intellij.ui.JBColor;
import fr.socolin.applicationinsights.TelemetryType;

import javax.swing.*;
import java.awt.*;

public class TelemetryTypeRender extends TelemetryRenderBase {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        TelemetryType type = (TelemetryType) value;

        super.setText(type.toString());

        switch (type) {
            case Message:
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.Message", JBColor.orange));
                break;
            case Request:
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.Request", JBColor.green));
                break;
            case Exception:
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.Exception", JBColor.red));
                break;
            case Metric:
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.Metric", JBColor.gray));
                break;
            case Event:
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.CustomEvents", JBColor.cyan));
                break;
            case RemoteDependency:
                super.setText("Dependency");
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.RemoteDependency", JBColor.blue));
                break;
            case Unk:
                super.setForeground(JBColor.namedColor("ApplicationInsights.TelemetryColor.Unk", JBColor.darkGray));
                break;
        }
        return this;
    }
}
