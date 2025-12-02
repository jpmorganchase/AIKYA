package com.aikya.orchestrator.repository.client;

public class TableColumnInfo {
    private String columnName;
    private String columnType;

    // Constructors, getters, and setters

    public TableColumnInfo(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}