package fr.socolin.applicationinsights.toolwindows;

import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Date;

public class TelemetryTableModel extends AbstractTableModel {
    private final String[] columnNames = new String[]{
            "timestamp", "type", "data"
    };
    private final Class<?>[] columnClass = new Class[]{
            Date.class, TelemetryType.class, Telemetry.class
    };
    private ArrayList<Telemetry> telemetries;

    public TelemetryTableModel(ArrayList<Telemetry> telemetries) {
        this.telemetries = telemetries;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClass[columnIndex];
    }

    @Override
    public int getRowCount() {
        return telemetries.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Telemetry telemetry = telemetries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return telemetry.getTimestamp();
            case 1:
                return telemetry.getType();
            case 2:
                return telemetry;
            default:
                return null;
        }
    }
}
