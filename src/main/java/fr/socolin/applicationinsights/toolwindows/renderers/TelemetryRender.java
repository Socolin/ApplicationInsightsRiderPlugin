package fr.socolin.applicationinsights.toolwindows.renderers;

import com.intellij.ui.JBColor;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.metricdata.*;

import javax.swing.*;
import java.awt.*;

public class TelemetryRender extends TelemetryRenderBase {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        super.setForeground(JBColor.foreground());

        Telemetry telemetry = (Telemetry) value;
        String text;
        switch (telemetry.getType()) {
            case Exception: {
                ExceptionData exceptionData = telemetry.getData(ExceptionData.class);

                colorComponentDependingOnSeverityLevel(exceptionData.severityLevel, isSelected);

                String message = exceptionData.exceptions.get(0).message;
                if (exceptionData.severityLevel != null) {
                    text = "[" + exceptionData.severityLevel + "] " + message;
                } else {
                    text = message;
                }
                break;
            }
            case Request: {
                RequestData requestData = telemetry.getData(RequestData.class);

                if (requestData.responseCode.startsWith("5")) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else if (requestData.responseCode.startsWith("4")) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Warning", JBColor.orange));
                } else {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
                }
                text = requestData.responseCode + " - " + requestData.name;
                break;
            }
            case Metric: {
                MetricData metricData = telemetry.getData(MetricData.class);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < metricData.metrics.size(); i++) {
                    MetricData.Metric metric = metricData.metrics.get(i);
                    if (i > 0)
                        sb.append(" - ");
                    sb.append(metric.name).append(':').append(metric.value);
                }
                text = sb.toString();
                break;
            }
            case RemoteDependency: {
                RemoteDependencyData remoteDependencyData = telemetry.getData(RemoteDependencyData.class);

                if (!remoteDependencyData.success) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.RemoteDependency.Failed", JBColor.red));
                } else {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.RemoteDependency.Success", JBColor.foreground()));
                }

                if (remoteDependencyData.type != null && remoteDependencyData.type.equals("SQL")) {
                    text = remoteDependencyData.type + " - " + remoteDependencyData.target + " - " + remoteDependencyData.data;
                } else if (remoteDependencyData.type != null && remoteDependencyData.type.equals("Http")) {
                    text = remoteDependencyData.type + " - " + remoteDependencyData.resultCode + " - " + remoteDependencyData.name;
                } else {
                    text = remoteDependencyData.type + " - " + remoteDependencyData.target + " - " + remoteDependencyData.data + " - " + remoteDependencyData.name;
                }
                break;
            }
            case Message: {
                MessageData messageData = telemetry.getData(MessageData.class);

                colorComponentDependingOnSeverityLevel(messageData.severityLevel, isSelected);
                if (messageData.severityLevel != null) {
                    text = "[" + messageData.severityLevel + "] " + messageData.message;
                } else {
                    text = messageData.message;
                }
                break;
            }
            case Event: {
                EventData eventData = telemetry.getData(EventData.class);
                text = eventData.name;
                break;
            }
            default:
                text = telemetry.toString();
                break;
        }

        super.setText(text);
        return this;
    }

    private void colorComponentDependingOnSeverityLevel(String severityLevel, boolean isSelected) {
        if ("Error".equals(severityLevel)) {
            super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
        } else if ("Warning".equals(severityLevel)) {
            super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Warning", JBColor.orange));
        } else if ("Critical".equals(severityLevel)) {
            super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
            if (!isSelected) {
                super.setBackground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Critical", new JBColor(0xA21319, 0x5B0006)));
            }
        } else {
            super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
        }
    }
}
