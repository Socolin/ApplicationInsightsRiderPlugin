package fr.socolin.applicationinsights.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugMessageListener implements XDebuggerManagerListener {
    private static final Logger log = Logger.getInstance("XDebuggerManagerListener");

    // XDebuggerManager.TOPIC
    public DebugMessageListener() {
        // com.intellij.xdebugger.XDebuggerManager.TOPIC;
        log.warn("create DebugMessageListener");
    }

    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
        log.warn("processStarted");
    }

    @Override
    public void processStopped(@NotNull XDebugProcess debugProcess) {
        log.warn("processStopped");
    }

    @Override
    public void currentSessionChanged(@Nullable XDebugSession previousSession, @Nullable XDebugSession currentSession) {
        log.warn("currentSessionChanged");
    }
}
