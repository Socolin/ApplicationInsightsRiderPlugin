package fr.socolin.applicationinsights.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.unscramble.AnalyzeStacktraceUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBUI;
import com.jetbrains.rd.util.lifetime.Lifetime;
import fr.socolin.applicationinsights.ApplicationInsightsSession;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryType;
import fr.socolin.applicationinsights.metricdata.*;
import fr.socolin.applicationinsights.settings.AppSettingState;
import fr.socolin.applicationinsights.ui.components.*;
import fr.socolin.applicationinsights.ui.renderers.TelemetryDateRender;
import fr.socolin.applicationinsights.ui.renderers.TelemetryRender;
import fr.socolin.applicationinsights.ui.renderers.TelemetryTypeRender;
import fr.socolin.applicationinsights.utils.TimeSpan;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppInsightsToolWindow {
    @NotNull
    private JPanel mainPanel;
    @NotNull
    private JBTable appInsightsLogsTable;
    @NotNull
    private JCheckBox metricCheckBox;
    @NotNull
    private JCheckBox exceptionCheckBox;
    @NotNull
    private JCheckBox messageCheckBox;
    @NotNull
    private JCheckBox dependencyCheckBox;
    @NotNull
    private JCheckBox requestCheckBox;
    @NotNull
    private JCheckBox eventCheckBox;
    @NotNull
    private JCheckBox pageViewCheckBox;
    @NotNull
    private JSplitPane splitPane;
    @NotNull
    private JLabel metricCounter;
    @NotNull
    private JLabel exceptionCounter;
    @NotNull
    private JLabel messageCounter;
    @NotNull
    private JLabel dependencyCounter;
    @NotNull
    private JLabel requestCounter;
    @NotNull
    private JLabel eventCounter;
    @NotNull
    private JLabel pageViewCounter;
    @NotNull
    private ColorBox metricColorBox;
    @NotNull
    private ColorBox exceptionColorBox;
    @NotNull
    private ColorBox messageColorBox;
    @NotNull
    private ColorBox dependencyColorBox;
    @NotNull
    private ColorBox requestColorBox;
    @NotNull
    private ColorBox eventColorBox;
    @NotNull
    private ColorBox pageViewColorBox;
    @NotNull
    private ExtendableTextField filter;
    @NotNull
    private JScrollPane logsScrollPane;
    @NotNull
    private ActionToolbarImpl toolbar;
    @NotNull
    private JComponent editorPanel;
    @NotNull
    private JPanel formattedTelemetryInfo;

    @NotNull
    private final Project project;
    @NotNull
    private final TelemetryRender telemetryRender;
    @NotNull
    private final Map<TelemetryType, Integer> telemetryCountPerType = new HashMap<>();
    @NotNull
    private final ApplicationInsightsSession applicationInsightsSession;
    private Lifetime lifetime;

    @NotNull
    private Editor editor;
    @NotNull
    private TelemetryTableModel telemetryTableModel;
    @NotNull
    private ArrayList<JLabel> telemetryTypesCounter = new ArrayList<>();
    @NotNull
    private Document jsonPreviewDocument;
    private boolean autoScrollToTheEnd;
    private final TextConsoleBuilder builder;


    public AppInsightsToolWindow(
            @NotNull ApplicationInsightsSession applicationInsightsSession,
            @NotNull Project project,
            Lifetime lifetime) {
        this.project = project;
        this.applicationInsightsSession = applicationInsightsSession;
        this.lifetime = lifetime;

        initTelemetryTypeFilters();

        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        try {
            ReadAction.nonBlocking(() -> {
                return CodeFoldingManager.getInstance(ProjectManager.getInstance().getDefaultProject()).buildInitialFoldings(jsonPreviewDocument);
            }).submit(AppExecutorUtil.getAppExecutorService()).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        this.telemetryRender = new TelemetryRender(lifetime);
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
        appInsightsLogsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        appInsightsLogsTable.getColumnModel().getColumn(2).setMaxWidth(100);
        appInsightsLogsTable.getTableHeader().setUI(null);

        filter.setExtensions(new ClearTextFieldExtension(filter));

        filter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                AppInsightsToolWindow.this.applicationInsightsSession.updateFilter(filter.getText());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                AppInsightsToolWindow.this.applicationInsightsSession.updateFilter(filter.getText());
            }
        });

        appInsightsLogsTable.getSelectionModel().addListSelectionListener(e -> {
            selectTelemetry(telemetryTableModel.getRow(appInsightsLogsTable.getSelectedRow()));
        });

        builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.filters(AnalyzeStacktraceUtil.EP_NAME.getExtensions(project));

        AppSettingState.getInstance().showFilteredIndicator.advise(lifetime, (v) -> {
            this.appInsightsLogsTable.invalidate();
            this.appInsightsLogsTable.repaint();
            return Unit.INSTANCE;
        });
    }

    private void selectTelemetry(@Nullable Telemetry telemetry) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        updateJsonPreview(gson.toJson(telemetry == null ? "" : telemetry.getJsonObject()));

        if (telemetry == null) {
            return;
        }

        formattedTelemetryInfo.removeAll();
        if (telemetry.getFilteredBy() != null) {
            formattedTelemetryInfo.add(new JLabel("This log was filtered by " + telemetry.getFilteredBy()), createConstraint(0, 0, 0));
        }

        int column = 1;
        if (telemetry.getTags().containsKey("ai.operation.id")) {
            formattedTelemetryInfo.add(createTitleLabel("OperationId"), createConstraint(0, column++, 0));
            String operationId = telemetry.getTags().get("ai.operation.id");
            JLabel jLabel = new JLabel("<html><a href=''>" + operationId + "</a></html>");
            jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            jLabel.addMouseListener(new ClickListener(e -> {
                applicationInsightsSession.updateFilter(operationId);
                this.filter.setText(operationId);
            }));
            formattedTelemetryInfo.add(jLabel, createConstraint(0, column++, 30));
        }
        if (telemetry.getType() == TelemetryType.Message) {
            formattedTelemetryInfo.add(createTitleLabel("Message"), createConstraint(0, column++, 0));
            MessageData messageData = telemetry.getData(MessageData.class);
            formattedTelemetryInfo.add(new JLabel(messageData.message), createConstraint(0, column++, 30));
        }
        if (telemetry.getType() == TelemetryType.Request) {
            formattedTelemetryInfo.add(createTitleLabel("Request"), createConstraint(0, column++, 0));
            RequestData requestData = telemetry.getData(RequestData.class);
            formattedTelemetryInfo.add(new JLabel(requestData.name), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Status code: " + requestData.responseCode), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Duration: " + new TimeSpan(requestData.duration).toString()), createConstraint(0, column++, 30));
        }
        if (telemetry.getType() == TelemetryType.Metric) {
            formattedTelemetryInfo.add(createTitleLabel("Metric"), createConstraint(0, column++, 0));
            MetricData metricData = telemetry.getData(MetricData.class);
            if (metricData.metrics != null) {
                for (MetricData.Metric metric : metricData.metrics) {
                    formattedTelemetryInfo.add(new JLabel(metric.name), createConstraint(0, column++, 30));
                    formattedTelemetryInfo.add(new JLabel("Kind: " + metric.kind), createConstraint(0, column++, 60));
                    formattedTelemetryInfo.add(new JLabel("Value: " + metric.value), createConstraint(0, column++, 60));
                    formattedTelemetryInfo.add(new JLabel("Count: " + metric.count), createConstraint(0, column++, 60));
                }
            }
        }
        if (telemetry.getType() == TelemetryType.RemoteDependency) {
            formattedTelemetryInfo.add(createTitleLabel("Dependency"), createConstraint(0, column++, 0));
            RemoteDependencyData remoteDependencyData = telemetry.getData(RemoteDependencyData.class);
            formattedTelemetryInfo.add(new JLabel("Success: " + remoteDependencyData.success), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Type: " + remoteDependencyData.type), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Target: " + remoteDependencyData.target), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Data " + remoteDependencyData.data), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("ResultCode: " + remoteDependencyData.resultCode), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Duration: " + new TimeSpan(remoteDependencyData.duration).toString()), createConstraint(0, column++, 30));
        }
        if (telemetry.getType() == TelemetryType.Event) {
            formattedTelemetryInfo.add(createTitleLabel("Event"), createConstraint(0, column++, 0));
            EventData eventData = telemetry.getData(EventData.class);
            formattedTelemetryInfo.add(new JLabel("Name: " + eventData.name), createConstraint(0, column++, 30));
        }
        if (telemetry.getType() == TelemetryType.PageView) {
            formattedTelemetryInfo.add(createTitleLabel("PageView"), createConstraint(0, column++, 0));
            PageViewData pageViewData = telemetry.getData(PageViewData.class);
            formattedTelemetryInfo.add(new JLabel("Name: " + pageViewData.name), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Duration: " + new TimeSpan(pageViewData.duration).toString()), createConstraint(0, column++, 30));
        }
        if (telemetry.getType() == TelemetryType.Exception) {
            formattedTelemetryInfo.add(createTitleLabel("Exception"), createConstraint(0, column++, 0));
            ExceptionData exceptionData = telemetry.getData(ExceptionData.class);
            var consoleView = builder.getConsole();
            consoleView.clear();
            consoleView.allowHeavyFilters();
            for (ExceptionData.ExceptionDetailData exception : exceptionData.exceptions) {
                consoleView.print(StackTraceFormatter.formatStackTrace(exception), ConsoleViewContentType.NORMAL_OUTPUT);
                formattedTelemetryInfo.add(consoleView.getComponent(), createConstraint(0, column++, 30));
            }
        }

        ITelemetryData telemetryData = telemetry.getData(ITelemetryData.class);
        if (telemetryData != null && telemetryData.getProperties() != null && !telemetryData.getProperties().isEmpty()) {
            formattedTelemetryInfo.add(createTitleLabel("Properties"), createConstraint(0, column++, 0));
            for (Map.Entry<String, String> entry : telemetryData.getProperties().entrySet()) {
                JLabel jLabel = new JLabel("<html>" + entry.getKey() + ": " + "<a href=''>" + entry.getValue() + "</a></html>");
                jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                jLabel.addMouseListener(new ClickListener(e -> {
                    applicationInsightsSession.updateFilter(entry.getValue());
                    this.filter.setText(entry.getValue());
                }));
                formattedTelemetryInfo.add(jLabel, createConstraint(0, column++, 30));
            }
        }

        // Padding
        {
            GridBagConstraints c = createConstraint(0, 10_000, 0);
            c.weighty = 1;
            formattedTelemetryInfo.add(new JPanel(), c);
        }

        formattedTelemetryInfo.revalidate();
        formattedTelemetryInfo.repaint();
    }

    @NotNull
    private JLabel createTitleLabel(String label) {
        JLabel title = new JLabel("<html><b>" + label + "</b></html>");
        Font font = title.getFont();
        font.deriveFont(Font.BOLD);
        title.setFont(font);
        return title;
    }

    @NotNull
    private GridBagConstraints createConstraint(int x, int y, int padX) {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.gridx = x;
        gridConstraints.gridy = y;
        gridConstraints.gridheight = 1;
        gridConstraints.gridwidth = 1;
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridConstraints.weightx = 1;
        gridConstraints.weighty = 0;
        gridConstraints.anchor = GridBagConstraints.NORTHEAST;
        gridConstraints.insets = JBUI.insetsLeft(padX);
        return gridConstraints;
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
        CodeFoldingManager.getInstance(ProjectManager.getInstance().getDefaultProject()).updateFoldRegions(editor);
    }

    private void initTelemetryTypeFilters() {
        metricCounter.putClientProperty("TelemetryType", TelemetryType.Metric);
        exceptionCounter.putClientProperty("TelemetryType", TelemetryType.Exception);
        messageCounter.putClientProperty("TelemetryType", TelemetryType.Message);
        dependencyCounter.putClientProperty("TelemetryType", TelemetryType.RemoteDependency);
        requestCounter.putClientProperty("TelemetryType", TelemetryType.Request);
        eventCounter.putClientProperty("TelemetryType", TelemetryType.Event);
        pageViewCounter.putClientProperty("TelemetryType", TelemetryType.PageView);

        telemetryTypesCounter.addAll(Arrays.asList(metricCounter, exceptionCounter, messageCounter, dependencyCounter, requestCounter, eventCounter, pageViewCounter));

        metricCheckBox.putClientProperty("TelemetryType", TelemetryType.Metric);
        exceptionCheckBox.putClientProperty("TelemetryType", TelemetryType.Exception);
        messageCheckBox.putClientProperty("TelemetryType", TelemetryType.Message);
        dependencyCheckBox.putClientProperty("TelemetryType", TelemetryType.RemoteDependency);
        requestCheckBox.putClientProperty("TelemetryType", TelemetryType.Request);
        eventCheckBox.putClientProperty("TelemetryType", TelemetryType.Event);
        pageViewCheckBox.putClientProperty("TelemetryType", TelemetryType.PageView);

        for (JCheckBox checkBox : new JCheckBox[]{metricCheckBox, exceptionCheckBox, messageCheckBox, dependencyCheckBox, requestCheckBox, eventCheckBox, pageViewCheckBox}) {
            TelemetryType telemetryType = (TelemetryType) checkBox.getClientProperty("TelemetryType");
            checkBox.setSelected(applicationInsightsSession.isTelemetryVisible(telemetryType));
            checkBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    applicationInsightsSession.setTelemetryFiltered(telemetryType, false);
                } else {
                    applicationInsightsSession.setTelemetryFiltered(telemetryType, true);
                }
            });
        }
    }

    @NotNull
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
            int index,
            @NotNull Telemetry telemetry,
            boolean visible,
            boolean shouldScroll
    ) {
        if (visible) {
            if (index != -1)
                telemetryTableModel.addRow(index, telemetry);
            else
                telemetryTableModel.addRow(telemetry);
            SwingUtilities.invokeLater(() -> {
                if (autoScrollToTheEnd && shouldScroll) {
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
        pageViewColorBox = new ColorBox(JBColor.namedColor("ApplicationInsights.TelemetryColor.PageView", JBColor.yellow));

        toolbar = createToolbar();
        toolbar.setTargetComponent(mainPanel);

        jsonPreviewDocument = new LanguageTextField.SimpleDocumentCreator().createDocument("", JsonLanguage.INSTANCE, project);
        editor = EditorFactory.getInstance().createViewer(jsonPreviewDocument, project, EditorKind.MAIN_EDITOR);
        if (editor instanceof EditorEx) {
            ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JsonFileType.INSTANCE));
            ((EditorEx) editor).getFoldingModel().setFoldingEnabled(true);
        }
        editor.getSettings().setIndentGuidesShown(true);
        editor.getSettings().setAdditionalLinesCount(3);
        editor.getSettings().setFoldingOutlineShown(true);
        editor.getSettings().setUseSoftWraps(PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.useSoftWrap"));

        editorPanel = editor.getComponent();
    }

    @NotNull
    private ActionToolbarImpl createToolbar() {
        final DefaultActionGroup actionGroup = new DefaultActionGroup();

        actionGroup.add(new OptionsToolbarAction(() -> toolbar));
        actionGroup.addSeparator();

        autoScrollToTheEnd = PropertiesComponent.getInstance().getBoolean("fr.socolin.application-insights.autoScrollToTheEnd");

        actionGroup.add(new AutoScrollToTheEndToolbarAction((selected) -> {
            autoScrollToTheEnd = selected;
            PropertiesComponent.getInstance().setValue("fr.socolin.application-insights.autoScrollToTheEnd", selected);
            if (autoScrollToTheEnd) {
                performAutoScrollToTheEnd();
            }
        }, autoScrollToTheEnd));

        actionGroup.add(new ToggleCaseInsensitiveSearchToolbarAction());

        actionGroup.add(new AbstractToggleUseSoftWrapsAction(SoftWrapAppliancePlaces.PREVIEW, false) {
            {
                ActionUtil.copyFrom(this, "EditorToggleUseSoftWraps");
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                super.setSelected(e, state);
                PropertiesComponent.getInstance().setValue("fr.socolin.application-insights.useSoftWrap", state);
            }

            @NotNull
            @Override
            protected Editor getEditor(@NotNull AnActionEvent e) {
                return editor;
            }
        });

        actionGroup.add(new ClearApplicationInsightsLogToolbarAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                applicationInsightsSession.clear();
            }
        });

        return new ActionToolbarImpl("ApplicationInsights", actionGroup, false);
    }

}

