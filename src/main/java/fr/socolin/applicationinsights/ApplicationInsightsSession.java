package fr.socolin.applicationinsights;

import com.intellij.ui.content.Content;
import com.intellij.util.IconUtil;
import com.jetbrains.rd.util.lifetime.LifetimeDefinition;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import fr.socolin.applicationinsights.toolwindows.AppInsightsToolWindow;
import kotlin.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ApplicationInsightsSession {
    public final List<Telemetry> telemetries = new ArrayList<>();
    public List<Telemetry> filteredTelemetries = new ArrayList<>();
    private TelemetryFactory telemetryFactory;
    private DotNetDebugProcess dotNetDebugProcess;
    private Set<TelemetryType> enabledTelemetryTypes = new HashSet<>();
    private String filter = "";
    private boolean firstMessage = true;
    @Nullable
    private AppInsightsToolWindow appInsightsToolWindow;

    public ApplicationInsightsSession(
            TelemetryFactory telemetryFactory,
            DotNetDebugProcess dotNetDebugProcess
    ) {
        this.telemetryFactory = telemetryFactory;
        this.dotNetDebugProcess = dotNetDebugProcess;

        this.enabledTelemetryTypes.add(TelemetryType.Message);
        this.enabledTelemetryTypes.add(TelemetryType.Request);
        this.enabledTelemetryTypes.add(TelemetryType.Exception);
        this.enabledTelemetryTypes.add(TelemetryType.Event);
        this.enabledTelemetryTypes.add(TelemetryType.RemoteDependency);
    }

    public void startListeningToOutputDebugMessage() {
        dotNetDebugProcess.getSessionProxy().getTargetDebug().advise(new LifetimeDefinition(), outputMessageWithSubject -> {
            Telemetry telemetry = telemetryFactory.tryCreateFromDebugOutputLog(outputMessageWithSubject.getOutput());
            if (telemetry != null) {
                addTelemetry(telemetry);
            }
            return Unit.INSTANCE;
        });
    }

    public void setTelemetryFiltered(TelemetryType telemetryType, boolean filtered) {
        if (filtered) {
            this.enabledTelemetryTypes.remove(telemetryType);
        } else {
            this.enabledTelemetryTypes.add(telemetryType);
        }
        updateFilteredTelemetries();
    }

    private void addTelemetry(Telemetry telemetry) {
        if (firstMessage) {
            firstMessage = false;

            appInsightsToolWindow = new AppInsightsToolWindow(this, dotNetDebugProcess.getProject());

            Content content = dotNetDebugProcess.getSession().getUI().createContent(
                    "appinsights",
                    appInsightsToolWindow.getContent(),
                    "Application Insights",
                    IconUtil.getAddIcon(), // FIXME replace with icon
                    null
            );
            dotNetDebugProcess.getSession().getUI().addContent(content);
        }

        boolean visible = false;
        synchronized (telemetries) {
            telemetries.add(telemetry);
            if (isVisible(telemetry)) {
                filteredTelemetries.add(telemetry);
                visible = true;
            }
        }
        if (appInsightsToolWindow != null)
            appInsightsToolWindow.addTelemetry(telemetry, visible);
    }

    private void updateFilteredTelemetries() {
        synchronized (telemetries) {
            filteredTelemetries = telemetries.stream()
                    .filter(this::isVisible)
                    .collect(Collectors.toList());
        }
        if (appInsightsToolWindow != null)
            appInsightsToolWindow.setTelemetries(telemetries, filteredTelemetries);
    }

    private boolean isVisible(Telemetry telemetry) {
        return enabledTelemetryTypes.contains(telemetry.getType()) && (filter.equals("") || telemetry.getJson().contains(filter));
    }

    public boolean isEnabled(TelemetryType telemetryType) {
        return this.enabledTelemetryTypes.contains(telemetryType);
    }

    public void updateFilter(String filter) {
        this.filter = filter;
        updateFilteredTelemetries();
    }
}
