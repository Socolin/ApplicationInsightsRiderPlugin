package fr.socolin.applicationinsights.settings.converters;

import com.google.gson.Gson;
import com.intellij.util.xmlb.Converter;
import com.jetbrains.rd.util.reactive.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringArrayPropertyConverter extends Converter<Property<String[]>> {
    @Override
    public @Nullable Property<String[]> fromString(@NotNull String s) {
        return new Property<>(new Gson().fromJson(s, String[].class));
    }

    @Override
    public @Nullable String toString(@NotNull Property<String[]> property) {
        return new Gson().toJson(property.getValue());
    }
}