package fr.socolin.applicationinsights.toolwindows.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fr.socolin.applicationinsights.ApplicationInsightsBundle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FilterIndicatorToolbarAction extends ToggleAction {
    private boolean state;
    private Consumer<Boolean> onSelect;

    public FilterIndicatorToolbarAction(Consumer<Boolean> onSelect, boolean selected) {
        super();
        this.onSelect = onSelect;
        state = selected;

        String message = ApplicationInsightsBundle.message("action.ApplicationInsights.ToggleFilteredIndicator.text");
        this.getTemplatePresentation().setDescription(message);
        this.getTemplatePresentation().setText(message);
        this.getTemplatePresentation().setIcon(AllIcons.General.Filter);
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
