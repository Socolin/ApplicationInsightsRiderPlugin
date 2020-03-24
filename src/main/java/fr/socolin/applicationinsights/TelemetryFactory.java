package fr.socolin.applicationinsights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.socolin.applicationinsights.metricdata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TelemetryFactory {
    private final JsonParser jsonParser;
    private final Gson gson;

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
        return new Telemetry(type, json, jsonObject, telemetryData);
    }

    @Nullable
    public Telemetry tryCreateFromDebugOutputLog(@NotNull String output) {
        if (!output.startsWith("category: Application Insights Telemetry")) {
            return null;
        }

        // FIXME: We should also parse what is in parenthesis (Filtered log, unconfigured)
        String json = output.substring(output.indexOf('{'), output.lastIndexOf('}') + 1);

        return fromJson(json);
    }
}
