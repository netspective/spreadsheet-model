package org.netspective.io.spreadsheet.model;

import org.apache.poi.ss.usermodel.Row;
import org.netspective.io.spreadsheet.template.Column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultTableRow implements TableRow
{
    private final Row dataRow;
    private final List<TableCell> cells;
    private final Map<Column, TableCell> mapOfCellsByColumn = new HashMap<Column, TableCell>();

    public DefaultTableRow(final Row dataRow, final List<TableCell> cells)
    {
        this.dataRow = dataRow;
        this.cells = Collections.unmodifiableList(new ArrayList<TableCell>(cells));

        for(final TableCell tc : this.cells)
            mapOfCellsByColumn.put(tc.getColumn(), tc);
    }

    public int getRowNumberInSheet()
    {
        return dataRow.getRowNum() + 1;
    }

    public Row getDataRow()
    {
        return dataRow;
    }

    public List<TableCell> getCells()
    {
        return cells;
    }

    public TableCell findCellForColumn(final Column column)
    {
        return mapOfCellsByColumn.get(column);
    }

    public String getLocatorForMessage()
    {
        return String.format("Row %d", getRowNumberInSheet());
    }

}