package fr.socolin.applicationinsights.toolwindows;

import fr.socolin.applicationinsights.Telemetry;
import fr.socolin.applicationinsights.TelemetryType;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class TelemetryTableModel extends AbstractTableModel {
    private final String[] columnNames = new String[]{
            "timestamp", "type", "data"
    };
    private final Class<?>[] columnClass = new Class[]{
            Date.class, TelemetryType.class, Telemetry.class
    };
    private List<Telemetry> telemetries;

    public TelemetryTableModel() {
        this.telemetries = new ArrayList<>();
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

    public void clear() {
        int size = this.telemetries.size();
        this.telemetries.clear();
        this.fireTableRowsDeleted(0, size - 1);
    }

    public void addRow(Telemetry telemetry) {
        this.telemetries.add(telemetry);
        this.fireTableRowsInserted(this.telemetries.size() - 1, this.telemetries.size() - 1);
    }

    public void setRows(List<Telemetry> telemetries) {
        int previousSize = this.telemetries.size();
        this.telemetries.clear();
        this.fireTableRowsDeleted(0, previousSize - 1);
        this.telemetries.addAll(telemetries);
        this.fireTableRowsInserted(0, this.telemetries.size() - 1);
    }

    @Nullable
    public Telemetry getRow(int selectedRow) {
        if (selectedRow < 0)
            return null;
        if (selectedRow >= this.getRowCount())
            return null;
        return telemetries.get(selectedRow);
    }
}
