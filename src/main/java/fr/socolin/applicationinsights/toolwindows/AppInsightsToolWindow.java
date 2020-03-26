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
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import fr.socolin.applicationinsights.ApplicationInsightsSession;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryType;
import fr.socolin.applicationinsights.metricdata.ExceptionData;
import fr.socolin.applicationinsights.toolwindows.components.ColorBox;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryDateRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryTypeRender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppInsightsToolWindow {
    private static final Logger log = Logger.getInstance("AppInsightsToolWindow");
    private final Project project;
    private Map<TelemetryType, Integer> telemetryCountPerType = new HashMap<>();

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
    private JLabel metricCounter;
    private JLabel exceptionCounter;
    private JLabel messageCounter;
    private JLabel dependencyCounter;
    private JLabel requestCounter;
    private JLabel eventCounter;
    private ColorBox metricColorBox;
    private ColorBox exceptionColorBox;
    private ColorBox messageColorBox;
    private ColorBox dependencyColorBox;
    private ColorBox requestColorBox;
    private ColorBox eventColorBox;

    private JCheckBox[] telemetryTypesCheckBoxes;
    private JLabel[] telemetryTypesCounter;
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

        metricCheckBox.setBorder(JBUI.Borders.emptyRight(8));
        exceptionCheckBox.setBorder(JBUI.Borders.emptyRight(8));
        messageCheckBox.setBorder(JBUI.Borders.emptyRight(8));
        dependencyCheckBox.setBorder(JBUI.Borders.emptyRight(8));
        requestCheckBox.setBorder(JBUI.Borders.emptyRight(8));
        eventCheckBox.setBorder(JBUI.Borders.emptyRight(8));

        metricCounter.putClientProperty("TelemetryType", TelemetryType.Metric);
        exceptionCounter.putClientProperty("TelemetryType", TelemetryType.Exception);
        messageCounter.putClientProperty("TelemetryType", TelemetryType.Message);
        dependencyCounter.putClientProperty("TelemetryType", TelemetryType.RemoteDependency);
        requestCounter.putClientProperty("TelemetryType", TelemetryType.Request);
        eventCounter.putClientProperty("TelemetryType", TelemetryType.Event);

        metricCounter.setBorder(JBUI.Borders.emptyRight(8));
        exceptionCounter.setBorder(JBUI.Borders.emptyRight(8));
        messageCounter.setBorder(JBUI.Borders.emptyRight(8));
        dependencyCounter.setBorder(JBUI.Borders.emptyRight(8));
        requestCounter.setBorder(JBUI.Borders.emptyRight(8));
        eventCounter.setBorder(JBUI.Borders.emptyRight(8));

        telemetryTypesCheckBoxes = new JCheckBox[]{metricCheckBox, exceptionCheckBox, messageCheckBox, dependencyCheckBox, requestCheckBox, eventCheckBox};
        telemetryTypesCounter = new JLabel[]{metricCounter, exceptionCounter, messageCounter, dependencyCounter, requestCounter, eventCounter};

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

    public void selectSession(@Nullable ApplicationInsightsSession applicationInsightsSession) {
        this.activeApplicationInsightsSession = applicationInsightsSession;
        if (applicationInsightsSession == null) {
            return;
        }

        telemetryTableModel = new TelemetryTableModel();
        telemetryTableModel.setRows(applicationInsightsSession.getFilteredTelemetries());
        applicationInsightsSession.registerChanges(this::addTelemetry, telemetryTableModel::setRows, this::setTelemetries);
        appInsightsLogsTable.setModel(telemetryTableModel);

        appInsightsLogsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        appInsightsLogsTable.getColumnModel().getColumn(0).setMaxWidth(130);
        appInsightsLogsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        appInsightsLogsTable.getColumnModel().getColumn(1).setMaxWidth(100);

        this.updateEnabledTelemetryTypeCheckBoxes(applicationInsightsSession);
    }

    private void setTelemetries(List<Telemetry> telemetries) {
        telemetryCountPerType.clear();
         for (Telemetry telemetry : telemetries) {
             telemetryCountPerType.compute(telemetry.getType(), (telemetryType, count) -> count == null ? 1 : count + 1);
         }
        updateTelemetryTypeCounter(null);
    }

    private void addTelemetry(Telemetry telemetry, Boolean visible) {
        if (visible) {
            telemetryTableModel.addRow(telemetry);
        }
        telemetryCountPerType.compute(telemetry.getType(), (telemetryType, count) -> count == null ? 1 : count + 1);
        updateTelemetryTypeCounter(telemetry.getType());
    }

    private void updateTelemetryTypeCounter(@Nullable TelemetryType type) {
        for (JLabel counter : telemetryTypesCounter) {
            TelemetryType telemetryType = (TelemetryType) counter.getClientProperty("TelemetryType");
            if (type != null && telemetryType != type)
                continue;;

            int count = telemetryCountPerType.get(telemetryType);
            if (count < 1000) {
                counter.setText(String.valueOf(count));
            } else if (count < 1000000) {
                counter.setText(count / 1000 + "K");
            } else {
                counter.setText(count / 1000 + "M");
            }
        }
    }

    private void updateEnabledTelemetryTypeCheckBoxes(@NotNull ApplicationInsightsSession applicationInsightsSession) {
        for (JCheckBox checkBox : telemetryTypesCheckBoxes) {
            TelemetryType telemetryType = (TelemetryType) checkBox.getClientProperty("TelemetryType");
            log.trace(telemetryType + " - " + applicationInsightsSession.isEnabled(telemetryType));
            checkBox.setSelected(applicationInsightsSession.isEnabled(telemetryType));
        }
    }

    private void createUIComponents() {
        editor = new LanguageTextField(JsonLanguage.INSTANCE, project, "");

        metricColorBox= new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Metric", JBColor.gray) );
        exceptionColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Exception", JBColor.red));
        messageColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Message", JBColor.orange));
        dependencyColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.RemoteDependency", JBColor.blue) );
        requestColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Request", JBColor.green));
        eventColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.CustomEvents", JBColor.cyan) );
    }
}
