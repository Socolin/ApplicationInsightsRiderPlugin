package fr.socolin.applicationinsights;

import com.intellij.ui.content.Content;
import com.intellij.util.IconUtil;
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

    public void selectSession(@Nullable XDebugSession debugSession) {
        if (debugSession == null)
            return;

        AppInsightsToolWindow appInsightsToolWindow = new AppInsightsToolWindow(debugSession.getProject());

        Content content = debugSession.getUI().createContent(
                "hello",
                appInsightsToolWindow.getContent(),
                "Application Insights",
                IconUtil.getAddIcon(),
                null
        );
        debugSession.getUI().addContent(content);

        ApplicationInsightsSession applicationInsightsSession = sessions.get(debugSession.getDebugProcess());
        appInsightsToolWindow.selectSession(applicationInsightsSession);
    }
}
