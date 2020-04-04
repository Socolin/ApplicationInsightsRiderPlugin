package fr.socolin.applicationinsights;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TelemetryType {
    Message("Message"),
    Request("Request"),
    Exception("Exception"),
    Metric("Metric"),
    Event("Event"),
    RemoteDependency("RemoteDependency"),
    Unk(null);

    @Nullable
    private String typeName;

    TelemetryType(@Nullable String typeName) {
        this.typeName = typeName;
    }

    @NotNull
    public static TelemetryType fromType(@NotNull String typeName) {
        String name = typeName.substring(typeName.lastIndexOf('.') + 1);
        for (TelemetryType type : TelemetryType.values()) {
            if (name.equals(type.typeName))
                return type;
        }
        return Unk;
    }
}
