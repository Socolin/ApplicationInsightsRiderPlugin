package fr.socolin.applicationinsights.listeners;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManagerListener;
import fr.socolin.applicationinsights.ApplicationInsightsSessionManager;
import org.jetbrains.annotations.NotNull;

public class DebugMessageListener implements XDebuggerManagerListener {
    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
        ApplicationInsightsSessionManager.getInstance().startSession(debugProcess);
    }
}
