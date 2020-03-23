package fr.socolin.applicationinsights;

public enum TelemetryType {
    Message("Message"),
    Request("Request"),
    Exception("Exception"),
    Metric("Metric"),
    Event("Event"),
    RemoteDependency("RemoteDependency"),
    Unk(null);

    private String typeName;

    private TelemetryType(String typeName) {

        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static TelemetryType fromType(String typeName) {
        String name = typeName.substring(typeName.lastIndexOf('.') + 1);
        for (TelemetryType type : TelemetryType.values()) {
            if (name.equals(type.typeName))
                return type;
        }
        return Unk;
    }
}
