package fr.socolin.applicationinsights.settings.converters;

import com.intellij.util.xmlb.Converter;
import com.jetbrains.rd.util.reactive.Property;
import fr.socolin.applicationinsights.settings.FilterTelemetryMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterTelemetryModePropertyConverter extends Converter<Property<FilterTelemetryMode>> {
    @Override
    public @Nullable Property<FilterTelemetryMode> fromString(@NotNull String s) {
        return new Property<>(FilterTelemetryMode.valueOf(s));
    }

    @Override
    public @Nullable String toString(@NotNull Property<FilterTelemetryMode> property) {
        return property.getValue().toString();
    }
}
