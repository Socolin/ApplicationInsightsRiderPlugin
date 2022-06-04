package fr.socolin.applicationinsights.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

public class ProjectSettingsConfigurable implements SearchableConfigurable {
    private final Project project;

    public ProjectSettingsConfigurable(Project project) {
        this.project = project;
    }

    private ProjectSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Application Insights: Settings";
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new ProjectSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        boolean modified = mySettingsComponent.getCaseInsensitiveFiltering() != settings.caseInsensitiveFiltering.getValue();
        modified |= !Arrays.equals(mySettingsComponent.getFilteredLogs(), settings.filteredLogs.getValue());
        return modified;
    }

    @Override
    public void apply() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        settings.caseInsensitiveFiltering.setValue(mySettingsComponent.getCaseInsensitiveFiltering());
        settings.filteredLogs.setValue(mySettingsComponent.getFilteredLogs());
    }

    @Override
    public void reset() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        mySettingsComponent.setCaseInsensitiveFiltering(settings.caseInsensitiveFiltering.getValue());
        mySettingsComponent.setFilteredLogs(settings.filteredLogs.getValue());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

    @Override
    public @NotNull
    @NonNls String getId() {
        return "fr.socolin.applicationinsights.settings.ProjectSettingsConfigurable";
    }
}
