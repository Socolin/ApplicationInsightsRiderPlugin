package fr.socolin.applicationinsights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import fr.socolin.applicationinsights.metricdata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TelemetryFactory {
    private final JsonParser jsonParser;
    private final Gson gson;
    private final String appInsightsLogPrefix = "category: Application Insights Telemetry";
    private final String filteredByPrefix = " (filtered by ";
    private final String unconfiguredPrefix = " (unconfigured) ";

    public TelemetryFactory() {
        jsonParser = new JsonParser();
        gson = new Gson();
    }

    public Telemetry fromJson(String json) {
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

        String name = jsonObject.get("name").getAsString();
        TelemetryType type = TelemetryType.fromType(name);

        JsonObject data = jsonObject.getAsJsonObject("data").getAsJsonObject("baseData");
        ITelemetryData telemetryData = null;
        switch (type) {
            case Message:
                telemetryData = gson.fromJson(data, MessageData.class);
                break;
            case Request:
                telemetryData = gson.fromJson(data, RequestData.class);
                break;
            case Exception:
                telemetryData = gson.fromJson(data, ExceptionData.class);
                break;
            case Metric:
                telemetryData = gson.fromJson(data, MetricData.class);
                break;
            case RemoteDependency:
                telemetryData = gson.fromJson(data, RemoteDependencyData.class);
                break;
            case Event:
                telemetryData = gson.fromJson(data, EventData.class);
                break;
            case Unk:
                break;
        }
        Type tagsMapType = new TypeToken<Map<String, String>>() {}.getType();
        return new Telemetry(type, json, jsonObject, telemetryData, gson.fromJson(jsonObject.get("tags"), tagsMapType));
    }

    @Nullable
    public Telemetry tryCreateFromDebugOutputLog(@NotNull String output) {
        if (!output.startsWith(appInsightsLogPrefix)) {
            return null;
        }

        // FIXME: We should also parse what is in parenthesis (Filtered log, unconfigured)
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
