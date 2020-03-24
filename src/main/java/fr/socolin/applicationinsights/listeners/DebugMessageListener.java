package fr.socolin.applicationinsights.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import fr.socolin.applicationinsights.ApplicationInsightsSessionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugMessageListener implements XDebuggerManagerListener {
    private static final Logger log = Logger.getInstance("XDebuggerManagerListener");

    // XDebuggerManager.TOPIC
    public DebugMessageListener() {
        log.warn("create DebugMessageListener");
    }

    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
        ApplicationInsightsSessionManager.getInstance().startSession(debugProcess);
    }

    @Override
    public void processStopped(@NotNull XDebugProcess debugProcess) {
        ApplicationInsightsSessionManager.getInstance().endSession(debugProcess);
    }

    @Override
    public void currentSessionChanged(@Nullable XDebugSession previousSession, @Nullable XDebugSession currentSession) {
        ApplicationInsightsSessionManager.getInstance().selectSession(currentSession);
    }
}
