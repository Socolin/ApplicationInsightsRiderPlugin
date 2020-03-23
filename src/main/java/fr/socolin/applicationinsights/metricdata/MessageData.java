package fr.socolin.applicationinsights.metricdata;

import java.util.HashMap;

public class MessageData implements ITelemetryData {
    public String severityLevel;
    public String message;
    public HashMap<String, String> properties;
}
