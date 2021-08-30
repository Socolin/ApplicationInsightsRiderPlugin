package fr.socolin.applicationinsights;

import com.google.gson.JsonObject;
import fr.socolin.applicationinsights.metricdata.ITelemetryData;
import fr.socolin.applicationinsights.metricdata.RemoteDependencyData;
import fr.socolin.applicationinsights.metricdata.RequestData;
import fr.socolin.applicationinsights.utils.TimeSpan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;

public class Telemetry {
    @NotNull
    private final TelemetryType type;
    @NotNull
    private final String json;
    @NotNull
    private final JsonObject jsonObject;
    @NotNull
    private final Date timestamp;
    @NotNull
    private final ITelemetryData data;
    @NotNull
    private final Map<String, String> tags;
    @Nullable
    private String filteredBy;
    private boolean unconfigured;

    public Telemetry(
            @NotNull TelemetryType type,
            @NotNull String json,
            @NotNull JsonObject jsonObject,
            @NotNull ITelemetryData data,
            @NotNull Map<String, String> tags
    ) {
        this.type = type;
        this.json = json;
        this.jsonObject = jsonObject;
        this.data = data;
        this.tags = tags;
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(jsonObject.get("time").getAsString());
        Instant i = Instant.from(ta);
        timestamp = Date.from(i);
    }

    @NotNull
    public TelemetryType getType() {
        return type;
    }

    @NotNull
    public Date getTimestamp() {
        return timestamp;
    }

    @NotNull
    public JsonObject getJsonObject() {
        return jsonObject;
    }

    @NotNull
    public String getJson() {
        return json;
    }

    public <T extends ITelemetryData> T getData(Class<T> clazz) {
        return (T) data;
    }

    public void setFilteredBy(@Nullable String filteredBy) {
        this.filteredBy = filteredBy;
    }

    @Nullable
    public String getFilteredBy() {
        return filteredBy;
    }

    public void setUnConfigured() {
        unconfigured = true;
    }

    public boolean isUnconfigured() {
        return unconfigured;
    }

    @NotNull
    public Map<String, String> getTags() {
        return tags;
    }

    public TimeSpan getDuration() {
        if (getType() == TelemetryType.Request) {
            RequestData requestData = getData(RequestData.class);
            return new TimeSpan(requestData.duration);
        } else if (getType() == TelemetryType.RemoteDependency) {
            RemoteDependencyData remoteDependencyData = getData(RemoteDependencyData.class);
            return new TimeSpan(remoteDependencyData.duration);
        }
        return TimeSpan.Zero;
    }
}
