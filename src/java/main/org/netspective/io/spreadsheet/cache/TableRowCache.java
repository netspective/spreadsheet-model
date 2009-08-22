package org.netspective.io.spreadsheet.cache;

import org.netspective.io.spreadsheet.model.TableRow;

import java.util.List;

public interface TableRowCache
{
    public String getCacheName();
    public void cache(final TableRow row);

    public TableRow findUniqueTableRow(final String value) throws NoRowsForCacheValueException, MultipleRowsForCacheValueException;
    public List<TableRow> findAllTableRows(final String value);

    public String createValueNotFoundErrorMessage(final String value);
    public String createMultipleValuesFoundErrorMessage(final String value, final List<TableRow> tableRowsFound);    
}