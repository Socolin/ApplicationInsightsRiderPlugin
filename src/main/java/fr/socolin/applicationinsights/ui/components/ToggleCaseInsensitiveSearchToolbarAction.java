package fr.socolin.applicationinsights.ui.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fr.socolin.applicationinsights.ApplicationInsightsBundle;
import fr.socolin.applicationinsights.settings.AppSettingState;
import org.jetbrains.annotations.NotNull;

public class ToggleCaseInsensitiveSearchToolbarAction extends ToggleAction {

    public ToggleCaseInsensitiveSearchToolbarAction() {
        super();

        String message = ApplicationInsightsBundle.message("action.ApplicationInsights.ToggleCaseInsensitive.text");
        this.getTemplatePresentation().setDescription(message);
        this.getTemplatePresentation().setText(message);
        this.getTemplatePresentation().setIcon(AllIcons.Actions.MatchCase);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent actionEvent) {
        return AppSettingState.getInstance().caseInsensitiveSearch.getValue();
    }

    public void setSelected(@NotNull AnActionEvent actionEvent, boolean state) {
        AppSettingState.getInstance().caseInsensitiveSearch.setValue(state);
    }
}
