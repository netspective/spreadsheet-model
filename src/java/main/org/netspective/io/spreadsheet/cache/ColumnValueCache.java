package org.netspective.io.spreadsheet.cache;

import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.Column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnValueCache implements TableRowCache
{
    private final Map<String, List<TableRow>> rowCache = new HashMap<String, List<TableRow>>();
    private final Column column;

    public ColumnValueCache(final Column column)
    {
        this.column = column;
    }

    public String getCacheName()
    {
        return column.getQualifiedColumnName();
    }

    public Column getColumn()
    {
        return column;
    }

    public void cache(final TableRow row)
    {
        final String cacheValue = (String) row.findCellForColumn(column).getValue("UNKNOWN");
        List<TableRow> rowsForColumnValues = rowCache.get(cacheValue);
        if(rowsForColumnValues == null)
        {
            rowsForColumnValues = new ArrayList<TableRow>();
            rowCache.put(cacheValue, rowsForColumnValues);
        }
        rowsForColumnValues.add(row);
    }

    public String createValueNotFoundErrorMessage(final String value)
    {
        return String.format("Unable to find any rows in column %s ('%s', # %d) with value '%s'.", column.getColumnLetters(), column.getQualifiedColumnName(), column.getColumnIndex(), value);
    }

    public String createMultipleValuesFoundErrorMessage(final String value, final List<TableRow> tableRowsFound)
    {
        return String.format("Found %d rows for column %s ('%s', # %d) with value '%s'. Only one expected..", tableRowsFound.size(), column.getColumnLetters(), column.getQualifiedColumnName(), column.getColumnIndex(), value);
    }

    public TableRow findUniqueTableRow(final String value) throws NoRowsForCacheValueException, MultipleRowsForCacheValueException
    {
        final List<TableRow> foundRows = findAllTableRows(value);
        if(foundRows == null)
            throw new NoRowsForCacheValueException(this, value);
        if(foundRows.size() != 1)
            throw new MultipleRowsForCacheValueException(this, value, foundRows);
        return foundRows.get(0);
    }

    public List<TableRow> findAllTableRows(final String value)
    {
        return rowCache.get(value);
    }
}
