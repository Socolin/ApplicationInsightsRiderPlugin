package fr.socolin.applicationinsights.toolwindows.components;

import javax.swing.*;
import java.awt.*;

public class ColorBox extends JPanel {
    private Color color;

    public ColorBox(Color color) {
        this.color = color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(color);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.dispose();
    }
}
