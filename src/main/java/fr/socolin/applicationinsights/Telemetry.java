package fr.socolin.applicationinsights;

import com.google.gson.JsonObject;
import fr.socolin.applicationinsights.metricdata.ITelemetryData;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class Telemetry {
    private final TelemetryType type;
    private final String json;
    private final JsonObject jsonObject;
    private final Date timestamp;
    private final ITelemetryData data;
    private String filteredBy;
    private boolean unconfigured;

    public Telemetry(TelemetryType type, String json, JsonObject jsonObject, ITelemetryData data) {
        this.type = type;
        this.json = json;
        this.jsonObject = jsonObject;
        this.data = data;
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(jsonObject.get("time").getAsString());
        Instant i = Instant.from(ta);
        timestamp = Date.from(i);
    }

    public TelemetryType getType() {
        return type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public String getJson() {
        return json;
    }

    public <T extends ITelemetryData> T getData(Class<T> clazz) {
        return (T)data;
    }

    public void setFilteredBy(String filteredBy) {
        this.filteredBy = filteredBy;
    }

    public String getFilteredBy() {
        return filteredBy;
    }

    public void setUnConfigured() {
        unconfigured = true;
    }

    public boolean isUnconfigured() {
        return unconfigured;
    }
}
