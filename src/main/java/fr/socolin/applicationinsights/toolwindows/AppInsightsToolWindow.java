package fr.socolin.applicationinsights.toolwindows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.JBUI;
import fr.socolin.applicationinsights.ApplicationInsightsSession;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryType;
import fr.socolin.applicationinsights.metricdata.ExceptionData;
import fr.socolin.applicationinsights.toolwindows.components.AutoScrollToTheEndToolbarAction;
import fr.socolin.applicationinsights.toolwindows.components.ColorBox;
import fr.socolin.applicationinsights.toolwindows.components.FilterIndicatorToolbarAction;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryDateRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryTypeRender;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppInsightsToolWindow {
    private static final Logger log = Logger.getInstance("AppInsightsToolWindow");
    private final Project project;
    private final TelemetryRender telemetryRender = new TelemetryRender();
    private final Map<TelemetryType, Integer> telemetryCountPerType = new HashMap<>();

    private JPanel mainPanel;
    private JTable appInsightsLogsTable;
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
    private JBTextField filter;
    private JScrollPane logsScrollPane;
    private ActionToolbarImpl toolbar;
    private JComponent editorPanel;

    @NotNull
    private JLabel[] telemetryTypesCounter;
    @NotNull
    private final ApplicationInsightsSession applicationInsightsSession;
    @NotNull
    private TelemetryTableModel telemetryTableModel;
    private boolean autoScrollToTheEnd;
    @NonNull
    private Editor editor;
    @NonNull
    private Document jsonPreviewDocument;


    public AppInsightsToolWindow(@NonNull ApplicationInsightsSession applicationInsightsSession, @NotNull Project project) {
        this.project = project;
        this.applicationInsightsSession = applicationInsightsSession;

        initTelemetryTypeFilters();

        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);

        CodeFoldingManager.getInstance(project).buildInitialFoldings(jsonPreviewDocument);

        appInsightsLogsTable.setDefaultRenderer(Telemetry.class, telemetryRender);
        appInsightsLogsTable.setDefaultRenderer(TelemetryType.class, new TelemetryTypeRender());
        appInsightsLogsTable.setDefaultRenderer(Date.class, new TelemetryDateRender());
        appInsightsLogsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        telemetryTableModel = new TelemetryTableModel();
        appInsightsLogsTable.setModel(telemetryTableModel);
        appInsightsLogsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        appInsightsLogsTable.getColumnModel().getColumn(0).setMaxWidth(130);
        appInsightsLogsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        appInsightsLogsTable.getColumnModel().getColumn(1).setMaxWidth(100);
        appInsightsLogsTable.getTableHeader().setUI(null);


        filter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (AppInsightsToolWindow.this.applicationInsightsSession != null)
                    AppInsightsToolWindow.this.applicationInsightsSession.updateFilter(filter.getText());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                AppInsightsToolWindow.this.applicationInsightsSession.updateFilter(filter.getText());
            }
        });
        appInsightsLogsTable.getSelectionModel().addListSelectionListener(e -> {
            Telemetry telemetry = telemetryTableModel.getRow(appInsightsLogsTable.getSelectedRow());
            if (telemetry == null) {
                updateJsonPreview("");
                return;
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            updateJsonPreview(gson.toJson(telemetry.getJsonObject()));

            if (telemetry.getType() == TelemetryType.Exception) {
                ExceptionData data = telemetry.getData(ExceptionData.class);
                for (ExceptionData.ExceptionDetailData exception : data.exceptions) {
                    boolean found = false;
                    for (ExceptionData.ExceptionDetailData.Stack stack : exception.parsedStack) {
                        if (stack.fileName != null) {
                            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + stack.fileName);
                            if (file != null) {
                                OpenSourceUtil.navigate(true, new OpenFileDescriptor(project, file, Integer.parseInt(stack.line), 0));
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

    private void updateJsonPreview(String text) {
        StringBuilder sb = new StringBuilder();
        // A bit hackish, replace leading space with tabs for better formatting. `gson` is not giving any option on pretty print (maybe need to try another one ?)
        for (String line : text.split("\n")) {
            int i;
            for (i = 0; i < line.length() && line.charAt(i) == ' '; i++) ;
            if (i > 0) {
                for (int j = 0; j < i / 2; j++)
                    sb.append('\t');
                sb.append(line.substring(i));
            } else {
                sb.append(line);
            }
            sb.append('\n');
        }
        ApplicationManager.getApplication().runWriteAction(() -> {
            jsonPreviewDocument.setText(sb.toString());
        });
        CodeFoldingManager.getInstance(project).updateFoldRegions(editor);
    }

    private void initTelemetryTypeFilters() {
        metricCheckBox.putClientProperty("TelemetryType", TelemetryType.Metric);
        exceptionCheckBox.putClientProperty("TelemetryType", TelemetryType.Exception);
        messageCheckBox.putClientProperty("TelemetryType", TelemetryType.Message);
        dependencyCheckBox.putClientProperty("TelemetryType", TelemetryType.RemoteDependency);
        requestCheckBox.putClientProperty("TelemetryType", TelemetryType.Request);
        eventCheckBox.putClientProperty("TelemetryType", TelemetryType.Event);

        metricCounter.putClientProperty("TelemetryType", TelemetryType.Metric);
        exceptionCounter.putClientProperty("TelemetryType", TelemetryType.Exception);
        messageCounter.putClientProperty("TelemetryType", TelemetryType.Message);
        dependencyCounter.putClientProperty("TelemetryType", TelemetryType.RemoteDependency);
        requestCounter.putClientProperty("TelemetryType", TelemetryType.Request);
        eventCounter.putClientProperty("TelemetryType", TelemetryType.Event);

        JCheckBox[] telemetryTypesCheckBoxes = new JCheckBox[]{metricCheckBox, exceptionCheckBox, messageCheckBox, dependencyCheckBox, requestCheckBox, eventCheckBox};
        telemetryTypesCounter = new JLabel[]{metricCounter, exceptionCounter, messageCounter, dependencyCounter, requestCounter, eventCounter};

        for (JCheckBox checkBox : telemetryTypesCheckBoxes) {
            TelemetryType telemetryType = (TelemetryType) checkBox.getClientProperty("TelemetryType");
            checkBox.setSelected(applicationInsightsSession.isEnabled(telemetryType));
            checkBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    applicationInsightsSession.setTelemetryFiltered(telemetryType, false);
                } else {
                    applicationInsightsSession.setTelemetryFiltered(telemetryType, true);
                }
            });
        }
    }

    public JPanel getContent() {
        return mainPanel;
    }

    public void setTelemetries(
            @NotNull List<Telemetry> telemetries,
            @NotNull List<Telemetry> visibleTelemetries
    ) {
        telemetryCountPerType.clear();
        for (Telemetry telemetry : telemetries) {
            telemetryCountPerType.compute(telemetry.getType(), (telemetryType, count) -> count == null ? 1 : count + 1);
        }
        updateTelemetryTypeCounter(null);
        telemetryTableModel.setRows(visibleTelemetries);
    }

    public void addTelemetry(
            @NotNull Telemetry telemetry,
            boolean visible
    ) {
        if (visible) {
            telemetryTableModel.addRow(telemetry);
            SwingUtilities.invokeLater(() -> {
                if (autoScrollToTheEnd) {
                    performAutoScrollToTheEnd();
                }
            });
        }
        telemetryCountPerType.compute(telemetry.getType(), (telemetryType, count) -> count == null ? 1 : count + 1);
        updateTelemetryTypeCounter(telemetry.getType());
    }

    private void performAutoScrollToTheEnd() {
        appInsightsLogsTable.scrollRectToVisible(appInsightsLogsTable.getCellRect(telemetryTableModel.getRowCount() - 1, 0, true));
    }

    private void updateTelemetryTypeCounter(@Nullable TelemetryType type) {
        for (JLabel counter : telemetryTypesCounter) {
            TelemetryType telemetryType = (TelemetryType) counter.getClientProperty("TelemetryType");
            if (type != null && telemetryType != type)
                continue;

            int count = telemetryCountPerType.computeIfAbsent(telemetryType, (e) -> 0);
            if (count < 1000) {
                counter.setText(String.valueOf(count));
            } else if (count < 1000000) {
                counter.setText(count / 1000 + "K");
            } else {
                counter.setText(count / 1000 + "M");
            }
        }
    }

    private void createUIComponents() {
        metricColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Metric", JBColor.gray));
        exceptionColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Exception", JBColor.red));
        messageColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Message", JBColor.orange));
        dependencyColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.RemoteDependency", JBColor.blue));
        requestColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.Request", JBColor.green));
        eventColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.CustomEvents", JBColor.cyan));

        toolbar = createToolbar();

        jsonPreviewDocument = new LanguageTextField.SimpleDocumentCreator().createDocument("", JsonLanguage.INSTANCE, project);
        editor = EditorFactory.getInstance().createEditor(jsonPreviewDocument, project, JsonFileType.INSTANCE, true);
        if (editor instanceof EditorEx) {
            ((EditorEx) editor).getFoldingModel().setFoldingEnabled(true);
        }
        editor.getSettings().setIndentGuidesShown(true);
        editor.getSettings().setAdditionalLinesCount(3);
        editor.getSettings().setFoldingOutlineShown(true);
        editor.getSettings().setUseSoftWraps(PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.useSoftWrap"));

        editorPanel = editor.getComponent();
    }

    private ActionToolbarImpl createToolbar() {
        final DefaultActionGroup actionGroup = new DefaultActionGroup();

        autoScrollToTheEnd = PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.autoScrollToTheEnd");

        actionGroup.add(new AutoScrollToTheEndToolbarAction((selected) -> {
            autoScrollToTheEnd = selected;
            PropertiesComponent.getInstance().setValue("fr.socolin.application-insights.autoScrollToTheEnd", selected);
            if (autoScrollToTheEnd) {
                performAutoScrollToTheEnd();
            }
        }, autoScrollToTheEnd));

        actionGroup.add(new FilterIndicatorToolbarAction((selected) -> {
            this.telemetryRender.setShowFilteredIndicator(selected);
            this.appInsightsLogsTable.invalidate();
            this.appInsightsLogsTable.repaint();
        }, PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.showFilteredIndicator")));

        actionGroup.add(new AbstractToggleUseSoftWrapsAction(SoftWrapAppliancePlaces.PREVIEW, false) {
            {
                ActionUtil.copyFrom(this, "EditorToggleUseSoftWraps");
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                super.setSelected(e, state);
                PropertiesComponent.getInstance().setValue("fr.socolin.application-insights.useSoftWrap", state);
            }

            @Nullable
            @Override
            protected Editor getEditor(@NotNull AnActionEvent e) {
                return editor;
            }
        });

        return new ActionToolbarImpl("ApplicationInsights", actionGroup, false);
    }
}
