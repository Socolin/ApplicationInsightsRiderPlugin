package fr.socolin.applicationinsights.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class ClickListener implements MouseListener {
    private final Consumer<MouseEvent> onClick;

    ClickListener(Consumer<MouseEvent> onClick) {

        this.onClick = onClick;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        onClick.accept(e);
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
}
