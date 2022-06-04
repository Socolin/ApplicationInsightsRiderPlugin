package fr.socolin.applicationinsights.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html#the-appsettingscomponent-class
@State(
        name = "fr.socolin.applicationinsights.settings.AppSettingsState",
        storages = @Storage("applicationInsights.xml")
)
public class ProjectSettingsState implements PersistentStateComponent<ProjectSettingsState> {
    public boolean caseInsensitiveFiltering = false;
    public String[] filteredLogs = new String[0];

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
    }

}
