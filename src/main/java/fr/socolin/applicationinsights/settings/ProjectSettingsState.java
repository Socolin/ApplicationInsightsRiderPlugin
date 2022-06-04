package fr.socolin.applicationinsights.settings;

import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.jetbrains.rd.util.lifetime.LifetimeDefinition;
import com.jetbrains.rd.util.reactive.Property;
import fr.socolin.applicationinsights.settings.converters.BooleanPropertyConverter;
import fr.socolin.applicationinsights.settings.converters.StringArrayPropertyConverter;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html#the-appsettingscomponent-class
@State(
        name = "fr.socolin.applicationinsights.settings.ProjectSettingsState",
        storages = @Storage("applicationInsights.xml")
)
public class ProjectSettingsState implements PersistentStateComponentWithModificationTracker<ProjectSettingsState> {
    private final SimpleModificationTracker tracker = new SimpleModificationTracker();

    @OptionTag(converter = BooleanPropertyConverter.class)
    public final Property<Boolean> caseInsensitiveFiltering = new Property<>(false);
    @OptionTag(converter = StringArrayPropertyConverter.class)
    public final Property<String[]> filteredLogs = new Property<>(new String[0]);

    public ProjectSettingsState() {
        registerAllPropertyToIncrementTrackerOnChanges(this);
    }

    public static ProjectSettingsState getInstance(Project project) {
        return project.getService(ProjectSettingsState.class);
    }

    @Nullable
    @Override
    public ProjectSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
        registerAllPropertyToIncrementTrackerOnChanges(state);
    }

    private void registerAllPropertyToIncrementTrackerOnChanges(@NotNull ProjectSettingsState state) {
        incrementTrackerWhenPropertyChanges(state.caseInsensitiveFiltering);
        incrementTrackerWhenPropertyChanges(state.filteredLogs);
    }

    private <T> void incrementTrackerWhenPropertyChanges(Property<T> property) {
        property.advise(new LifetimeDefinition(), v -> {
            this.tracker.incModificationCount();
            return Unit.INSTANCE;
        });
    }

    @Override
    public long getStateModificationCount() {
        return this.tracker.getModificationCount();
    }
}
