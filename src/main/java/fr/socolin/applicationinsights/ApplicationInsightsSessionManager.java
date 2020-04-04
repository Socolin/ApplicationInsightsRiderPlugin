package fr.socolin.applicationinsights;

import com.intellij.xdebugger.XDebugProcess;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApplicationInsightsSessionManager {
    @Nullable
    private static ApplicationInsightsSessionManager instance;
    @NotNull
    private final TelemetryFactory telemetryFactory = new TelemetryFactory();

    @NotNull
    public static ApplicationInsightsSessionManager getInstance() {
        if (instance == null)
            instance = new ApplicationInsightsSessionManager();
        return instance;
    }

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
        applicationInsightsSession.startListeningToOutputDebugMessage();

        return applicationInsightsSession;
    }
}
