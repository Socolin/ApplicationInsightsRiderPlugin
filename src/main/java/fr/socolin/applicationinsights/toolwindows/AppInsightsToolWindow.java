package fr.socolin.applicationinsights.toolwindows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.rider.debugger.DotNetDebugProcess;
import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryFactory;
import fr.socolin.applicationinsights.TelemetryType;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryDateRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryRender;
import fr.socolin.applicationinsights.toolwindows.renderers.TelemetryTypeRender;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

public class AppInsightsToolWindow {
    private static final Logger log = Logger.getInstance("AppInsightsToolWindow");
    private final Project project;
    private final ToolWindow toolWindow;

    private final TelemetryFactory telemetryFactory;

    private JPanel mainPanel;
    private JTable appInsightsLogsTable;
    private JButton refreshButton;
    private EditorTextField editor;
    private ArrayList<Telemetry> telemetries;


    public AppInsightsToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.telemetryFactory = new TelemetryFactory();

        editor.setOneLineMode(false);
        editor.setFileType(JsonFileType.INSTANCE);

        appInsightsLogsTable.setDefaultRenderer(TelemetryType.class, new TelemetryTypeRender());
        appInsightsLogsTable.setDefaultRenderer(Date.class, new TelemetryDateRender());
        appInsightsLogsTable.setDefaultRenderer(Telemetry.class, new TelemetryRender());
        appInsightsLogsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        appInsightsLogsTable.getSelectionModel().addListSelectionListener(e -> {
            if (appInsightsLogsTable.getSelectedRow() < 0) {
                Document document = EditorFactory.getInstance().createDocument("");
                editor.setDocument(document);
                return;
            }
            if (appInsightsLogsTable.getSelectedRow() >= telemetries.size()) {
                Document document = EditorFactory.getInstance().createDocument("");
                editor.setDocument(document);
                return;
            }

            Telemetry telemetry = telemetries.get(appInsightsLogsTable.getSelectedRow());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Document document = EditorFactory.getInstance().createDocument(gson.toJson(telemetry.getJsonObject()));
            editor.setDocument(document);
        });

        refreshButton.addActionListener(e -> {
            XDebugSession debugSession = XDebuggerManager.getInstance(project).getCurrentSession();
            if (debugSession == null)
                return;
            XDebugProcess process = debugSession.getDebugProcess();
            DotNetDebugProcess dotNetDebugProcess = (DotNetDebugProcess) process;

            String debugOutputContent = dotNetDebugProcess.getDebuggerOutputConsole().getText();
            String[] lines = debugOutputContent.split("\r?\n");

/*
            ArrayList<String> lines = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(AppInsightsToolWindow.class.getResourceAsStream("/test-output.txt")));
            String l;
            try {
                while ((l = bufferedReader.readLine()) != null) {
                    lines.add(l);
                }
            } catch (IOException ex) {

            }
*/
            ArrayList<Telemetry> telemetries = new ArrayList<>();


            for (String line : lines) {
                if (!line.startsWith("category: Application Insights Telemetry")) {
                    continue;
                }
                String json = line.substring(line.indexOf('{'), line.lastIndexOf('}') + 1);
                telemetries.add(telemetryFactory.fromJson(json));
            }
            appInsightsLogsTable.setModel(new TelemetryTableModel(telemetries));
            appInsightsLogsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
            appInsightsLogsTable.getColumnModel().getColumn(0).setMaxWidth(90);
            appInsightsLogsTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            appInsightsLogsTable.getColumnModel().getColumn(1).setMaxWidth(90);

            this.telemetries = telemetries;
        });

    }


    public JPanel getContent() {
        return mainPanel;
    }

    private void createUIComponents() {
        editor = new LanguageTextField(JsonLanguage.INSTANCE, project, "");
    }
}
