package fr.socolin.applicationinsights.settings.converters;

import com.intellij.util.xmlb.Converter;
import com.jetbrains.rd.util.reactive.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanPropertyConverter extends Converter<Property<Boolean>> {
    @Override
    public @Nullable Property<Boolean> fromString(@NotNull String s) {
        return new Property<>(s.equals("true"));
    }

    @Override
    public @Nullable String toString(@NotNull Property<Boolean> booleanProperty) {
        return booleanProperty.getValue().toString();
    }
}
