package fr.socolin.applicationinsights.toolwindows;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class AppInsightsToolWindow {
    private JPanel mainPanel;
    private JTable table1;

    public AppInsightsToolWindow(ToolWindow toolWindow) {

    }


    public JPanel getContent() {
        return mainPanel;
    }
}
