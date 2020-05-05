package fr.socolin.applicationinsights;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.content.Content;
import com.jetbrains.rd.util.lifetime.LifetimeDefinition;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import fr.socolin.applicationinsights.ui.AppInsightsToolWindow;
import kotlin.Unit;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationInsightsSession {
    @NotNull
    private static final Icon appInsightsIcon = IconLoader.getIcon("/icons/application-insights.svg");
    @NotNull
    private final TelemetryFactory telemetryFactory;
    @NotNull
    private final DotNetDebugProcess dotNetDebugProcess;
    @NotNull
    private final List<Telemetry> telemetries = new ArrayList<>();
    @NotNull
    private final List<Telemetry> filteredTelemetries = new ArrayList<>();
    @NotNull
    private final Set<TelemetryType> visibleTelemetryTypes = new HashSet<>();
    @NotNull
    private String filter = "";
    @Nullable
    private AppInsightsToolWindow appInsightsToolWindow;
    private boolean firstMessage = true;

    public ApplicationInsightsSession(
            @NotNull TelemetryFactory telemetryFactory,
            @NotNull DotNetDebugProcess dotNetDebugProcess
    ) {
        this.telemetryFactory = telemetryFactory;
        this.dotNetDebugProcess = dotNetDebugProcess;

        this.visibleTelemetryTypes.add(TelemetryType.Message);
        this.visibleTelemetryTypes.add(TelemetryType.Request);
        this.visibleTelemetryTypes.add(TelemetryType.Exception);
        this.visibleTelemetryTypes.add(TelemetryType.Event);
        this.visibleTelemetryTypes.add(TelemetryType.RemoteDependency);
        this.visibleTelemetryTypes.add(TelemetryType.Unk);
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

    public void setTelemetryFiltered(
            @NotNull TelemetryType telemetryType,
            boolean hidden
    ) {
        if (hidden) {
            this.visibleTelemetryTypes.remove(telemetryType);
        } else {
            this.visibleTelemetryTypes.add(telemetryType);
        }
        updateFilteredTelemetries();
    }

    public boolean isTelemetryVisible(@NotNull TelemetryType telemetryType) {
        return this.visibleTelemetryTypes.contains(telemetryType);
    }

    public void updateFilter(@NonNull String filter) {
        this.filter = filter;
        updateFilteredTelemetries();
    }

    public void clear() {
        this.telemetries.clear();
        updateFilteredTelemetries();
    }

    private void addTelemetry(@NotNull Telemetry telemetry) {
        if (firstMessage) {
            firstMessage = false;

            appInsightsToolWindow = new AppInsightsToolWindow(this, dotNetDebugProcess.getProject());

            Content content = dotNetDebugProcess.getSession().getUI().createContent(
                    "appinsights",
                    appInsightsToolWindow.getContent(),
                    "Application Insights",
                    appInsightsIcon,
                    null
            );
            dotNetDebugProcess.getSession().getUI().addContent(content);
        }

        boolean visible = false;
        synchronized (telemetries) {
            telemetries.add(telemetry);
            if (isTelemetryVisible(telemetry)) {
                filteredTelemetries.add(telemetry);
                visible = true;
            }
        }
        if (appInsightsToolWindow != null)
            appInsightsToolWindow.addTelemetry(telemetry, visible);
    }

    private void updateFilteredTelemetries() {
        synchronized (telemetries) {
            filteredTelemetries.clear();
            filteredTelemetries.addAll(
                    telemetries.stream()
                            .filter(this::isTelemetryVisible)
                            .collect(Collectors.toList())
            );
        }
        if (appInsightsToolWindow != null)
            appInsightsToolWindow.setTelemetries(telemetries, filteredTelemetries);
    }

    private boolean isTelemetryVisible(@NotNull Telemetry telemetry) {
        return visibleTelemetryTypes.contains(telemetry.getType()) && (filter.equals("") || telemetry.getJson().contains(filter));
    }
}
