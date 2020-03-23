package fr.socolin.applicationinsights.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class AppInsightsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        AppInsightsToolWindow appInsightsToolWindow = new AppInsightsToolWindow(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(appInsightsToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
