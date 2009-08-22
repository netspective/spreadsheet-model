package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;

public class StringArrayValueHandler implements ValueHandler
{
    private final StringValueHandler underlyingValueHandler;
    private final String splitRegEx;

    public StringArrayValueHandler(final StringValueHandler underlyingValueHandler, final String splitRegEx)
    {
        this.splitRegEx = splitRegEx;
        this.underlyingValueHandler = underlyingValueHandler;
    }

    public boolean isBlank(final Cell cell)
    {
        return underlyingValueHandler.isBlank(cell);
    }

    public boolean isValid(final Cell cell, final StringBuilder unassignableValueAsText)
    {
        return underlyingValueHandler.isValid(cell, unassignableValueAsText);
    }

    public Object getValue(final Cell cell, final Object defaultValue)
    {
        final Object string = underlyingValueHandler.getValue(cell, null);
        if(string == null)
            return defaultValue;

        return ((String) string).split(splitRegEx);
    }
}
