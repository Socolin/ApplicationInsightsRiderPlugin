package fr.socolin.applicationinsights.toolwindows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.OpenSourceUtil;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import fr.socolin.applicationinsights.*;
import fr.socolin.applicationinsights.metricdata.ExceptionData;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryDateRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryTypeRender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Date;

public class AppInsightsToolWindow {
    private static final Logger log = Logger.getInstance("AppInsightsToolWindow");
    private final Project project;

    private JPanel mainPanel;
    private JTable appInsightsLogsTable;
    private EditorTextField editor;
    private JCheckBox metricCheckBox;
    private JCheckBox exceptionCheckBox;
    private JCheckBox messageCheckBox;
    private JCheckBox dependencyCheckBox;
    private JCheckBox requestCheckBox;
    private JCheckBox eventCheckBox;
    private JSplitPane splitPane;
    private JCheckBox[] telemetryTypesCheckBoxes;
    @Nullable
    private ApplicationInsightsSession activeApplicationInsightsSession;
    private TelemetryTableModel telemetryTableModel;


    public AppInsightsToolWindow(Project project) {
        this.project = project;

        initTelemetryTypeFilters();

        editor.setOneLineMode(false);
        editor.setFileType(JsonFileType.INSTANCE);

        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);

        appInsightsLogsTable.setDefaultRenderer(TelemetryType.class, new TelemetryTypeRender());
        appInsightsLogsTable.setDefaultRenderer(Date.class, new TelemetryDateRender());
        appInsightsLogsTable.setDefaultRenderer(Telemetry.class, new TelemetryRender());
        appInsightsLogsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        appInsightsLogsTable.getSelectionModel().addListSelectionListener(e -> {
            Telemetry telemetry = telemetryTableModel.getRow(appInsightsLogsTable.getSelectedRow());
            if (telemetry == null) {
                Document document = EditorFactory.getInstance().createDocument("");
                editor.setDocument(document);
                return;
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Document document = EditorFactory.getInstance().createDocument(gson.toJson(telemetry.getJsonObject()));
            editor.setDocument(document);

            if (telemetry.getType() == TelemetryType.Exception) {
                ExceptionData data = telemetry.getData(ExceptionData.class);
                for (ExceptionData.ExceptionDetailData exception : data.exceptions) {
                    boolean found = false;
                    for (ExceptionData.ExceptionDetailData.Stack stack : exception.parsedStack) {
                        if (stack.fileName != null) {
                            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + stack.fileName);
                            if (file != null) {
                                OpenSourceUtil.navigate(true, new OpenFileDescriptor(project, file, Integer.parseInt(stack.line),0));
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found)
                        break;
                }
            }
        });
    }

    private void initTelemetryTypeFilters() {
        metricCheckBox.putClientProperty("TelemetryType", TelemetryType.Metric);
        exceptionCheckBox.putClientProperty("TelemetryType", TelemetryType.Exception);
        messageCheckBox.putClientProperty("TelemetryType", TelemetryType.Message);
        dependencyCheckBox.putClientProperty("TelemetryType", TelemetryType.RemoteDependency);
        requestCheckBox.putClientProperty("TelemetryType", TelemetryType.Request);
        eventCheckBox.putClientProperty("TelemetryType", TelemetryType.Event);

        telemetryTypesCheckBoxes = new JCheckBox[]{metricCheckBox, exceptionCheckBox, messageCheckBox, dependencyCheckBox, requestCheckBox, eventCheckBox};

        for (JCheckBox checkBox : telemetryTypesCheckBoxes) {
            TelemetryType telemetryType = (TelemetryType) checkBox.getClientProperty("TelemetryType");
            checkBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (activeApplicationInsightsSession != null)
                        activeApplicationInsightsSession.setTelemetryFiltered(telemetryType, false);
                } else {
                    if (activeApplicationInsightsSession != null)
                        activeApplicationInsightsSession.setTelemetryFiltered(telemetryType, true);
                }
            });
        }
    }

    public JPanel getContent() {
        return mainPanel;
    }

    private void createUIComponents() {
        editor = new LanguageTextField(JsonLanguage.INSTANCE, project, "");
    }

    public void selectSession(@Nullable ApplicationInsightsSession applicationInsightsSession) {
        this.activeApplicationInsightsSession = applicationInsightsSession;
        if (applicationInsightsSession == null) {
            return;
        }

        telemetryTableModel = new TelemetryTableModel();
        telemetryTableModel.setRows(applicationInsightsSession.getFilteredTelemetries());
        applicationInsightsSession.registerChanges(telemetryTableModel::addRow, telemetryTableModel::setRows);
        appInsightsLogsTable.setModel(telemetryTableModel);

        appInsightsLogsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        appInsightsLogsTable.getColumnModel().getColumn(0).setMaxWidth(90);
        appInsightsLogsTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        appInsightsLogsTable.getColumnModel().getColumn(1).setMaxWidth(90);

        this.updateEnabledTelemetryTypeCheckBoxes(applicationInsightsSession);
    }

    private void updateEnabledTelemetryTypeCheckBoxes(@NotNull ApplicationInsightsSession applicationInsightsSession) {
        for (JCheckBox checkBox : telemetryTypesCheckBoxes) {
            TelemetryType telemetryType = (TelemetryType) checkBox.getClientProperty("TelemetryType");
            log.trace(telemetryType + " - " + applicationInsightsSession.isEnabled(telemetryType));
            checkBox.setSelected(applicationInsightsSession.isEnabled(telemetryType));
        }
    }
}
