package fr.socolin.applicationinsights.toolwindows.renderers;

import com.google.gson.JsonObject;
import com.intellij.ui.JBColor;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.metricdata.EventData;
import fr.socolin.applicationinsights.metricdata.MessageData;
import fr.socolin.applicationinsights.metricdata.MetricData;
import fr.socolin.applicationinsights.metricdata.RemoteDependencyData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TelemetryRender extends TelemetryRenderBase {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        super.setForeground(JBColor.foreground());

        Telemetry telemetry = (Telemetry) value;
        JsonObject data = telemetry.getJsonObject().get("data").getAsJsonObject();
        switch (telemetry.getType()) {
            case Exception: {
                JsonObject baseData = data.get("baseData").getAsJsonObject();
                String severityLevel = baseData.get("severityLevel").getAsString();
                if ("Error".equals(severityLevel)) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else if ("Warning".equals(severityLevel)) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
                }
                String message = baseData.get("exceptions").getAsJsonArray().get(0).getAsJsonObject().get("message").getAsString();
                super.setText(severityLevel + " - " + message);
                break;
            }
            case Request: {
                JsonObject baseData = data.get("baseData").getAsJsonObject();
                String operationName = baseData.get("name").getAsString();
                String responseCode = baseData.get("responseCode").getAsString();
                if (responseCode.startsWith("5")) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Error", JBColor.red));
                } else if (responseCode.startsWith("4")) {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Warning", JBColor.orange));
                } else {
                    super.setForeground(JBColor.namedColor("ApplicationInsights.SeverityLevel.Default", JBColor.foreground()));
                }
                super.setText(responseCode + " - " + operationName);
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
                    super.setText(remoteDependencyData.type + " - " + remoteDependencyData.target  + " - " + remoteDependencyData.data);
                }
                else if (remoteDependencyData.type != null && remoteDependencyData.type.equals("Http")) {
                    super.setText(remoteDependencyData.type + " - " +  remoteDependencyData.resultCode + " - " + remoteDependencyData.name);
                } else {
                    super.setText(remoteDependencyData.type + " - " + remoteDependencyData.target  + " - " + remoteDependencyData.data + " - " + remoteDependencyData.name);
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
