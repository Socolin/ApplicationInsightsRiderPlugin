package fr.socolin.applicationinsights;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import fr.socolin.applicationinsights.toolwindows.AppInsightsToolWindow;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ApplicationInsightsSessionManager {
    private static ApplicationInsightsSessionManager instance;
    private TelemetryFactory telemetryFactory = new TelemetryFactory();
    @Nullable
    private AppInsightsToolWindow appInsightsToolWindow;

    public static ApplicationInsightsSessionManager getInstance() {
        if (instance == null)
            instance = new ApplicationInsightsSessionManager();
        return instance;
    }

    private Map<XDebugProcess, ApplicationInsightsSession> sessions = new HashMap<>();

    private ApplicationInsightsSessionManager() {
    }

    @Nullable
    public ApplicationInsightsSession startSession(XDebugProcess debugProcess) {
        if (!(debugProcess instanceof DotNetDebugProcess))
            return null;

        ApplicationInsightsSession applicationInsightsSession = new ApplicationInsightsSession(
                telemetryFactory,
                (DotNetDebugProcess) debugProcess
        );
        sessions.put(debugProcess, applicationInsightsSession);
        applicationInsightsSession.startListeningToOutputDebugMessage();
        return applicationInsightsSession;
    }

    public void endSession(XDebugProcess debugProcess) {

    }

    public void selectSession(XDebugSession debugSession) {
        if (this.appInsightsToolWindow != null) {
            ApplicationInsightsSession applicationInsightsSession = sessions.get(debugSession.getDebugProcess());
            this.appInsightsToolWindow.selectSession(applicationInsightsSession);
        }
    }

    public void registerUi(AppInsightsToolWindow appInsightsToolWindow) {
        this.appInsightsToolWindow = appInsightsToolWindow;
    }
}
