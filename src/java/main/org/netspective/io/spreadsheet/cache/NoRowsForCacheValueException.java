package org.netspective.io.spreadsheet.cache;

public class NoRowsForCacheValueException extends Exception
{
    private final TableRowCache cache;
    private final String value;

    public NoRowsForCacheValueException(final TableRowCache cache, final String value)
    {
        super(cache.createValueNotFoundErrorMessage(value));
        this.cache = cache;
        this.value = value;
    }

    public TableRowCache getIndexDefn()
    {
        return cache;
    }

    public String getValue()
    {
        return value;
    }
}
