package org.netspective.io.spreadsheet.model;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.value.ValueHandler;

public class DefaultTableCell implements TableCell
{
    private final ValueHandler valueHandler;
    private final Cell cell;
    private final Column column;

    public DefaultTableCell(final Column column, final Cell cell)
    {
        this.valueHandler = column.getValueHandler();
        this.column = column;
        this.cell = cell;
    }

    public Cell getCell()
    {
        return cell;
    }

    public boolean isBlankCell()
    {
        return getCell().getCellType() == Cell.CELL_TYPE_BLANK;
    }

    public Column getColumn()
    {
        return column;
    }

    public Object getValue()
    {
        return getValue(null);
    }

    public Object getValue(final Object defaultIfBlank)
    {
        return valueHandler.getValue(getCell(), defaultIfBlank);
    }

    public Object getValue(final Object defaultIfBlank, final Object defaultIfError)
    {
        return valueHandler.getValue(getCell(), defaultIfBlank);
    }
}