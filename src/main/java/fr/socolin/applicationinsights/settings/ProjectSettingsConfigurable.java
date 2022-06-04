package fr.socolin.applicationinsights.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

public class ProjectSettingsConfigurable implements Configurable {
    private final Project project;

    public ProjectSettingsConfigurable(Project project) {

        this.project = project;
    }

    private ProjectSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

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
        boolean modified = mySettingsComponent.getCaseInsensitiveFiltering() != settings.caseInsensitiveFiltering;
        modified |= !Arrays.equals(mySettingsComponent.getFilteredLogs(), settings.filteredLogs);
        // modified |= mySettingsComponent.getIdeaUserStatus() != settings.ideaStatus;
        return modified;
    }

    @Override
    public void apply() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        settings.caseInsensitiveFiltering = mySettingsComponent.getCaseInsensitiveFiltering();
        settings.filteredLogs = mySettingsComponent.getFilteredLogs();
    }

    @Override
    public void reset() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        mySettingsComponent.setCaseInsensitiveFiltering(settings.caseInsensitiveFiltering);
        mySettingsComponent.setFilteredLogs(settings.filteredLogs);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
