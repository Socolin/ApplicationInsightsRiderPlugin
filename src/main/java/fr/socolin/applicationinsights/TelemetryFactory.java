package fr.socolin.applicationinsights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import fr.socolin.applicationinsights.metricdata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public class TelemetryFactory {
    @NotNull
    private final Gson gson;

    public TelemetryFactory() {
        gson = new Gson();
    }

    @NotNull
    public Telemetry fromJson(@NotNull String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        String name = jsonObject.get("name").getAsString();
        TelemetryType type = TelemetryType.fromType(name);

        JsonObject data = jsonObject.getAsJsonObject("data").getAsJsonObject("baseData");

        ITelemetryData telemetryData = switch (type) {
            case Message -> gson.fromJson(data, MessageData.class);
            case Request -> gson.fromJson(data, RequestData.class);
            case Exception -> gson.fromJson(data, ExceptionData.class);
            case Metric -> gson.fromJson(data, MetricData.class);
            case RemoteDependency -> gson.fromJson(data, RemoteDependencyData.class);
            case Event -> gson.fromJson(data, EventData.class);
            default -> gson.fromJson(data, UnkData.class);
        };

        Type tagsMapType = new TypeToken<Map<String, String>>() {}.getType();

        return new Telemetry(type, json, jsonObject, telemetryData, gson.fromJson(jsonObject.get("tags"), tagsMapType));
    }

    @Nullable
    public Telemetry tryCreateFromDebugOutputLog(@NotNull String output) {
        @NotNull String appInsightsLogPrefix = "category: Application Insights Telemetry";
        @NotNull String filteredByPrefix = " (filtered by ";
        @NotNull String unconfiguredPrefix = " (unconfigured) ";

        if (!output.startsWith(appInsightsLogPrefix)) {
            return null;
        }

        String json = output.substring(output.indexOf('{'), output.lastIndexOf('}') + 1);

        Telemetry telemetry = fromJson(json);
        String telemetryState = output.substring(appInsightsLogPrefix.length());
        if (telemetryState.startsWith(filteredByPrefix)) {
            telemetry.setFilteredBy(telemetryState.substring(filteredByPrefix.length(), telemetryState.indexOf(')')));
        } else if (telemetryState.startsWith(unconfiguredPrefix)) {
            telemetry.setUnConfigured();
        }

        return telemetry;
    }
}
