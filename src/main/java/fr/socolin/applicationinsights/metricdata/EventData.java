package fr.socolin.applicationinsights.metricdata;

import java.util.HashMap;

public class EventData implements ITelemetryData {
    public String name;
    public HashMap<String, String> properties;

    @Override
    public HashMap<String, String> getProperties() {
        return properties;
    }
}
