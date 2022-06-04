package fr.socolin.applicationinsights.ui.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fr.socolin.applicationinsights.ApplicationInsightsBundle;
import fr.socolin.applicationinsights.settings.AppSettingState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FilterIndicatorToolbarAction extends ToggleAction {
    public FilterIndicatorToolbarAction() {
        super();

        String message = ApplicationInsightsBundle.message("action.ApplicationInsights.ToggleFilteredIndicator.text");
        this.getTemplatePresentation().setDescription(message);
        this.getTemplatePresentation().setText(message);
        this.getTemplatePresentation().setIcon(AllIcons.General.Filter);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent actionEvent) {
        return AppSettingState.getInstance().showFilteredIndicator.getValue();
    }

    public void setSelected(@NotNull AnActionEvent actionEvent, boolean state) {
        AppSettingState.getInstance().showFilteredIndicator.setValue(state);
    }
}
