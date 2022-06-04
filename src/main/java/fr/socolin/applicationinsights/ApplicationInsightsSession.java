package fr.socolin.applicationinsights;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.content.Content;
import com.jetbrains.rd.util.lifetime.LifetimeDefinition;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import fr.socolin.applicationinsights.settings.ProjectSettingsState;
import fr.socolin.applicationinsights.ui.AppInsightsToolWindow;
import kotlin.Unit;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationInsightsSession {
    @NotNull
    private static final Icon appInsightsIcon = IconLoader.getIcon("/icons/application-insights.svg", ApplicationInsightsSession.class);
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
    private boolean caseInsensitiveSearch;
    @NotNull
    private String filter = "";
    private String filterLowerCase = "";
    @Nullable
    private AppInsightsToolWindow appInsightsToolWindow;
    private boolean firstMessage = true;
    private boolean sortByDuration;
    private final ProjectSettingsState projectSettingsState;

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

        projectSettingsState = ProjectSettingsState.getInstance(dotNetDebugProcess.getProject());

        this.sortByDuration = PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.displayDurationColumn");
        this.caseInsensitiveSearch = PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.caseInsensitive");
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
        this.filterLowerCase = filter.toLowerCase(Locale.ROOT);
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

        int index = -1;
        boolean visible = false;
        synchronized (telemetries) {
            telemetries.add(telemetry);
            if (isTelemetryVisible(telemetry)) {
                if (sortByDuration) {
                    index = Collections.binarySearch(filteredTelemetries, telemetry, Comparator.comparing(Telemetry::getDuration));
                    if (index < 0) index = ~index;
                    filteredTelemetries.add(index, telemetry);
                } else
                    filteredTelemetries.add(telemetry);
                visible = true;
            }
        }
        if (appInsightsToolWindow != null)
            appInsightsToolWindow.addTelemetry(index, telemetry, visible, !sortByDuration);
    }

    private void updateFilteredTelemetries() {
        synchronized (telemetries) {
            filteredTelemetries.clear();
            Stream<Telemetry> stream = telemetries.stream()
                    .filter(this::isTelemetryVisible);
            if (sortByDuration)
                stream = stream.sorted(Comparator.comparing(Telemetry::getDuration));

            filteredTelemetries.addAll(stream.collect(Collectors.toList()));
        }
        if (appInsightsToolWindow != null)
            appInsightsToolWindow.setTelemetries(telemetries, filteredTelemetries);
    }

    private boolean isTelemetryVisible(@NotNull Telemetry telemetry) {
        if (!visibleTelemetryTypes.contains(telemetry.getType()))
            return false;

        for (String filteredLog : projectSettingsState.filteredLogs) {
            if (projectSettingsState.caseInsensitiveFiltering) {
                if (telemetry.getLowerCaseJson().contains(filteredLog.toLowerCase()))
                    return false;
            } else {
                if (telemetry.getJson().contains(filteredLog))
                    return false;
            }
        }

        if (!filter.isEmpty()) {
            if (caseInsensitiveSearch)
                return telemetry.getLowerCaseJson().toLowerCase().contains(filterLowerCase);
            else
                return telemetry.getJson().contains(filter);
        }

        return true;
    }

    public void toggleSortByDuration(boolean sortByDuration) {
        this.sortByDuration = sortByDuration;
        this.updateFilteredTelemetries();
    }

    public void toggleCaseSensitiveSearch(boolean caseInsensitiveSearch) {
        this.caseInsensitiveSearch = caseInsensitiveSearch;
        this.updateFilteredTelemetries();
    }
}
