package fr.socolin.applicationinsights.metricdata;

import java.util.HashMap;

public interface ITelemetryData {
    HashMap<String, String> getProperties();
}
