package fr.socolin.applicationinsights.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class ListenerTest implements ProjectManagerListener {
    private static final Logger log = Logger.getInstance("XDebuggerManagerListener");

    @Override
    public void projectOpened(@NotNull Project project) {
        log.warn("projectOpened " + project.getName());
    }
}
