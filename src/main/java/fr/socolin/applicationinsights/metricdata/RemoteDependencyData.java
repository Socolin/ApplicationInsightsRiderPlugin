package fr.socolin.applicationinsights.metricdata;

import java.util.HashMap;

public class RemoteDependencyData implements ITelemetryData {
    public String name;
    public String id;
    public String duration;
    public String resultCode;
    public String data;
    public boolean success;
    public String type;
    public String target;
    public HashMap<String, String> properties;

    @Override
    public HashMap<String, String> getProperties() {
        return properties;
    }
}
