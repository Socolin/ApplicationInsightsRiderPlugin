package fr.socolin.applicationinsights.ui.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fr.socolin.applicationinsights.ApplicationInsightsBundle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ToggleDurationToolbarAction extends ToggleAction {
    private boolean state;
    private Consumer<Boolean> onSelect;

    public ToggleDurationToolbarAction(Consumer<Boolean> onSelect, boolean selected) {
        super();
        this.onSelect = onSelect;
        state = selected;

        String message = ApplicationInsightsBundle.message("action.ApplicationInsights.ToggleDisplayDuration.text");
        this.getTemplatePresentation().setDescription(message);
        this.getTemplatePresentation().setText(message);
        this.getTemplatePresentation().setIcon(AllIcons.RunConfigurations.SortbyDuration);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent actionEvent) {
        return state;
    }

    public void setSelected(@NotNull AnActionEvent actionEvent, boolean state) {
        this.state = state;
        this.onSelect.accept(state);
    }
}
