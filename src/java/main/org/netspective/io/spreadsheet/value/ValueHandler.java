package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;

public interface ValueHandler
{
    public boolean isBlank(final Cell cell);
    public boolean isValid(final Cell cell, final StringBuilder unassignableValueAsText);
    public Object getValue(final Cell cell, final Object defaultValue);
}