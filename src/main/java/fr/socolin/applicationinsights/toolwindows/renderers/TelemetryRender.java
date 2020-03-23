package fr.socolin.applicationinsights.toolwindows.renderers;

import com.google.gson.JsonObject;
import com.intellij.ui.JBColor;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.metricdata.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TelemetryRender extends TelemetryRenderBase {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        super.setForeground(JBColor.foreground());

        Telemetry telemetry = (Telemetry) value;
        switch (telemetry.getType()) {
            case Exception: {
                ExceptionData exceptionData = telemetry.getData(ExceptionData.class);

                if ("Error".equals(exceptionData.severityLevel)) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else if ("Warning".equals(exceptionData.severityLevel)) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
                }

                String message = exceptionData.exceptions.get(0).message;
                super.setText(exceptionData.severityLevel + " - " + message);
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
                super.setText(requestData.responseCode + " - " + requestData.name);
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
                super.setText(sb.toString());
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
                    super.setText(remoteDependencyData.type + " - " + remoteDependencyData.target + " - " + remoteDependencyData.data);
                } else if (remoteDependencyData.type != null && remoteDependencyData.type.equals("Http")) {
                    super.setText(remoteDependencyData.type + " - " + remoteDependencyData.resultCode + " - " + remoteDependencyData.name);
                } else {
                    super.setText(remoteDependencyData.type + " - " + remoteDependencyData.target + " - " + remoteDependencyData.data + " - " + remoteDependencyData.name);
                }
                break;
            }
            case Message: {
                MessageData messageData = telemetry.getData(MessageData.class);

                if ("Error".equals(messageData.severityLevel)) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else if ("Warning".equals(messageData.severityLevel)) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
                }

                super.setText(messageData.severityLevel + " - " + messageData.message);
                break;
            }
            case Event: {
                EventData eventData = telemetry.getData(EventData.class);
                super.setText(eventData.name);
                break;
            }
            default:
                super.setText(telemetry.toString());
                break;
        }

        return this;
    }
}
