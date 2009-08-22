package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.List;

public class IntegerArrayValueHandler implements ValueHandler
{
    private final StringArrayValueHandler underlyingValueHandler;

    public IntegerArrayValueHandler(final StringArrayValueHandler underlyingValueHandler)
    {
        this.underlyingValueHandler = underlyingValueHandler;
    }

    public boolean isBlank(final Cell cell)
    {
        return underlyingValueHandler.isBlank(cell);
    }

    public boolean isValid(final Cell cell, final StringBuilder unassignableValueAsText)
    {
        if(! underlyingValueHandler.isValid(cell, unassignableValueAsText))
            return false;

        final Object object = underlyingValueHandler.getValue(cell, null);
        if(object == null)
            return true;

        final String[] strings = ((String[]) object);
        final List<Integer> invalidValues = new ArrayList<Integer>();
        int index = 0;
        for(final String s : strings)
        {
            try
            {
                Integer.parseInt(s);
            }
            catch (NumberFormatException e)
            {
                invalidValues.add(index);
            }
            index++;
        }

        if(invalidValues.size() > 0)
        {
            for(final int invalid : invalidValues)
            {
                final String textValue = strings[invalid];
                if(unassignableValueAsText.length() > 0)
                    unassignableValueAsText.append(", ");
                unassignableValueAsText.append(textValue);
            }
            return false;
        }
        else
            return true;
    }

    public Object getValue(final Cell cell, final Object defaultValue)
    {
        final Object string = underlyingValueHandler.getValue(cell, null);
        if(string == null)
            return defaultValue;

        final Object object = underlyingValueHandler.getValue(cell, null);
        if(object == null)
            return true;

        final String[] strings = ((String[]) object);
        final List<Integer> ints = new ArrayList<Integer>();
        for(final String s : strings)
        {
            try
            {
                ints.add(Integer.parseInt(s));
            }
            catch (NumberFormatException e)
            {
                ints.add((Integer) defaultValue);
            }
        }
        return ints.toArray(new Integer[ints.size()]);
    }
}
