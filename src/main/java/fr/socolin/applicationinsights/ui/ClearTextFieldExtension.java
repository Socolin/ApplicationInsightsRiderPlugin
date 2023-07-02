package fr.socolin.applicationinsights.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.TextIcon;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;

public class ClearTextFieldExtension implements ExtendableTextComponent.Extension {
    private final ExtendableTextField textField;

    public ClearTextFieldExtension(ExtendableTextField textField) {

        this.textField = textField;
    }

    @Override
    public Icon getIcon(boolean hovered) {
        return hovered ? AllIcons.Actions.CloseHovered : AllIcons.Actions.Close;
    }

    @Override
    public String getTooltip() {
        return "Clear";
    }

    @Override
    public Runnable getActionOnClick(@NotNull InputEvent inputEvent) {
        return textField.getText().isEmpty() ? null : () -> textField.setText(null);
    }
}
