package org.netspective.io.spreadsheet.model;

import org.apache.poi.ss.usermodel.Row;
import org.netspective.io.spreadsheet.template.Column;

import java.util.List;

public interface TableRow
{
    public int getRowNumberInSheet();
    public Row getDataRow();
    public List<TableCell> getCells();
    public TableCell findCellForColumn(final Column column);
    public String getLocatorForMessage();
}
