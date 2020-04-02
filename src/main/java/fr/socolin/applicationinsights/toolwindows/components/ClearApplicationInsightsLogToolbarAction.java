package fr.socolin.applicationinsights.toolwindows.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import fr.socolin.applicationinsights.ApplicationInsightsBundle;

public abstract class ClearApplicationInsightsLogToolbarAction extends AnAction {
    public ClearApplicationInsightsLogToolbarAction() {
        String message = ApplicationInsightsBundle.message("action.ApplicationInsights.ClearLogs.text");
        this.getTemplatePresentation().setDescription(message);
        this.getTemplatePresentation().setText(message);
        this.getTemplatePresentation().setIcon(AllIcons.Actions.GC);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
