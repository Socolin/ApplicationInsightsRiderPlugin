package fr.socolin.applicationinsights.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectSettingsComponent {

    private final JPanel minPanel;
    private final JBCheckBox caseInsensitiveFiltering = new JBCheckBox("Case insensitive log filtering");
    private final JBTextArea filteredLogs = new JBTextArea(10, 150);

    public ProjectSettingsComponent() {
        minPanel = FormBuilder.createFormBuilder()
                // .addLabeledComponent(new JBLabel("Enter user name: "), myUserNameText, 1, false)
                .addComponent(caseInsensitiveFiltering, 1)
                .addComponent(new JBLabel("<html>Hidden logs<br>It will hide all logs matching the given text, based on the JSON of the telemetry message.<br>You can find the json used to match a log in the Debug Output tab (while debugging)<br>One filter per line</html>"), 1)
                .addComponentFillVertically(filteredLogs, 0)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return minPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return caseInsensitiveFiltering;
    }

    @NotNull
    public String[] getFilteredLogs() {
        return Arrays.stream(filteredLogs.getText().split("\n")).filter(f -> !f.isEmpty()).toArray(String[]::new);
    }

    public void setFilteredLogs(@NotNull String[] newText) {
        filteredLogs.setText(String.join("\n", newText));
    }

    public boolean getCaseInsensitiveFiltering() {
        return caseInsensitiveFiltering.isSelected();
    }

    public void setCaseInsensitiveFiltering(boolean value) {
        caseInsensitiveFiltering.setSelected(value);
    }
}
