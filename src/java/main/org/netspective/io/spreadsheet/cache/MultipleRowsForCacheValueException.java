package org.netspective.io.spreadsheet.cache;

import org.netspective.io.spreadsheet.model.TableRow;

import java.util.List;

public class MultipleRowsForCacheValueException extends Exception
{
    private final TableRowCache cache;
    private final String value;
    private final List<TableRow> tableRowsFound;

    public MultipleRowsForCacheValueException(final TableRowCache cache, final String value, final List<TableRow> tableRowsFound)
    {
        super(cache.createMultipleValuesFoundErrorMessage(value, tableRowsFound));
        this.cache = cache;
        this.value = value;
        this.tableRowsFound = tableRowsFound;
    }

    public TableRowCache getIndexDefn()
    {
        return cache;
    }

    public String getValue()
    {
        return value;
    }

    public List<TableRow> getTableRowsFound()
    {
        return tableRowsFound;
    }
}
