package fr.socolin.applicationinsights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.socolin.applicationinsights.metricdata.*;

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

        JsonObject data = jsonObject.getAsJsonObject("data").getAsJsonObject("baseData");
        TelemetryType type = TelemetryType.fromType(name);
        ITelemetryData telemetryData = null;
        switch (type) {
            case Message:
                telemetryData = gson.fromJson(data, MessageData.class);
                break;
            case Request:
                break;
            case Exception:
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

    /*
* {
    "name": "Microsoft.ApplicationInsights.Dev.Request",
    "time": "2020-03-22T17:55:15.7817170Z",
    "tags": {
        "ai.application.ver": "1.0.0.0",
        "ai.cloud.roleInstance": "daelyr",
        "ai.operation.id": "03b66f60f1f12541aa86d36d433743d0",
        "ai.operation.name": "GET /",
        "ai.location.ip": "::1",
        "ai.internal.sdkVersion": "aspnet5c:2.13.1",
        "ai.internal.nodeName": "daelyr.(none)"
    },
    "data": {
        "baseType": "RequestData",
        "baseData": {
            "ver": 2,
            "id": "018420f051f5b844",
            "name": "GET /",
            "duration": "00:00:00.0004083",
            "success": false,
            "responseCode": "404",
            "url": "http://localhost:5000/",
            "properties": {
                "AspNetCoreEnvironment": "Development",
                "_MS.ProcessedByMetricExtractors": "(Name:'Requests', Ver:'1.1')",
                "DeveloperMode": "true"
            }
        }
    }
}*/

}
