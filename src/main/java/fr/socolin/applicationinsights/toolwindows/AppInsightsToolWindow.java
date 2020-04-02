package fr.socolin.applicationinsights.toolwindows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.JBUI;
import fr.socolin.applicationinsights.ApplicationInsightsSession;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryType;
import fr.socolin.applicationinsights.metricdata.*;
import fr.socolin.applicationinsights.toolwindows.components.AutoScrollToTheEndToolbarAction;
import fr.socolin.applicationinsights.toolwindows.components.ClearApplicationInsightsLogToolbarAction;
import fr.socolin.applicationinsights.toolwindows.components.ColorBox;
import fr.socolin.applicationinsights.toolwindows.components.FilterIndicatorToolbarAction;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryDateRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryTypeRender;
import fr.socolin.applicationinsights.utils.TimeSpan;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private JPanel formattedTelemetryInfo;

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
                selectTelemetry(null);
                return;
            }
            selectTelemetry(telemetry);
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
        if (telemetry.getTags() != null && telemetry.getTags().containsKey("ai.operation.id")) {
            formattedTelemetryInfo.add(createTitleLabel("OperationId"), createConstraint(0, column++, 0));
            JLabel jLabel = new JLabel("<html><a href=''>" + telemetry.getTags().get("ai.operation.id") + "</a></html>");
            jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            jLabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    applicationInsightsSession.updateFilter(telemetry.getTags().get("ai.operation.id"));
                    filter.setText(telemetry.getTags().get("ai.operation.id"));
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
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
            formattedTelemetryInfo.add(new JLabel("Status code:" + requestData.responseCode), createConstraint(0, column++, 30));
            formattedTelemetryInfo.add(new JLabel("Duration: " + new TimeSpan(requestData.duration).toString()), createConstraint(0, column++, 30));
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
        if (telemetry.getType() == TelemetryType.Exception) {
            formattedTelemetryInfo.add(createTitleLabel("Exception"), createConstraint(0, column++, 0));
            ExceptionData exceptionData = telemetry.getData(ExceptionData.class);
            for (ExceptionData.ExceptionDetailData exception : exceptionData.exceptions) {
                formattedTelemetryInfo.add(new JLabel(exception.message), createConstraint(0, column++, 30));
                for (ExceptionData.ExceptionDetailData.Stack stack : exception.parsedStack) {
                    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + stack.fileName);
                    JLabel jLabel;
                    if (file != null) {
                        formattedTelemetryInfo.add(new JLabel("<html>" + stack.method + "</html>"), createConstraint(0, column++, 60));
                        jLabel = new JLabel("<html><a href=''>" + stack.fileName + ":" + stack.line + "</a></html>");
                        jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        jLabel.addMouseListener(createMouseListenerNavigateToStack(stack, file));
                        formattedTelemetryInfo.add(jLabel, createConstraint(0, column++, 90));
                    } else {
                        formattedTelemetryInfo.add(new JLabel("<html>" + stack.method + "</html>"), createConstraint(0, column++, 60));
                        if (stack.fileName != null) {
                            formattedTelemetryInfo.add(new JLabel("<html><a href=''>" + stack.fileName + ":" + stack.line + "</a></html>"), createConstraint(0, column++, 90));
                        }
                    }
                }
            }
        }

        ITelemetryData telemetryData = telemetry.getData(ITelemetryData.class);
        if (telemetryData != null && telemetryData.getProperties() != null && telemetryData.getProperties().size() > 0) {
            formattedTelemetryInfo.add(createTitleLabel("Properties"), createConstraint(0, column++, 0));
            for (Map.Entry<String, String> entry : telemetryData.getProperties().entrySet()) {
                formattedTelemetryInfo.add(new JLabel(entry.getKey() + ": " + entry.getValue()), createConstraint(0, column++, 30));
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
    private MouseListener createMouseListenerNavigateToStack(ExceptionData.ExceptionDetailData.Stack stack, @NotNull VirtualFile file) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                    OpenSourceUtil.navigate(true, new OpenFileDescriptor(project, file, Integer.parseInt(stack.line), 0));
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }

    @NotNull
    private JLabel createTitleLabel(String label) {
        JLabel title = new JLabel("<html><b>" + label + "</b></html>");
        Font f = title.getFont();
        f.deriveFont(Font.BOLD);
        title.setFont(f);
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

        actionGroup.add(new ClearApplicationInsightsLogToolbarAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                applicationInsightsSession.clear();
            }
        });

        return new ActionToolbarImpl("ApplicationInsights", actionGroup, false);
    }
}
