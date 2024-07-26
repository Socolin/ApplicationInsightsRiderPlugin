package fr.socolin.applicationinsights.metricdata;

import java.util.HashMap;

public class PageViewData implements ITelemetryData {
    public String ver;
    public String name;
    public String duration;
    public HashMap<String, String> properties;

    @Override
    public HashMap<String, String> getProperties() {
        return properties;
    }
}
